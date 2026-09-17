package br.com.prismaapi.service.previsao;

import br.com.prismaapi.enums.MesDoAno;
import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.SituacaoDespesaRecorrente;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.exceptions.RequisicaoInvalidaException;
import br.com.prismaapi.model.dto.previsao.BaseCalculoPrevisaoDTO;
import br.com.prismaapi.model.dto.previsao.CenarioPrevisao;
import br.com.prismaapi.model.dto.previsao.MenorSaldoDTO;
import br.com.prismaapi.model.dto.previsao.MesPrevisaoDTO;
import br.com.prismaapi.model.dto.previsao.PrevisaoDTO;
import br.com.prismaapi.model.entity.cartao.Cartao;
import br.com.prismaapi.model.entity.conta.Conta;
import br.com.prismaapi.model.entity.despesarecorrente.DespesaRecorrente;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.despesarecorrente.DespesaRecorrenteRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.service.fatura.FaturaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrevisaoService {

    private final FaturaService faturaService;
    private static final int MESES_DA_BASE = 3;
    private final ContaRepository contaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final DespesaRecorrenteRepository despesaRecorrenteRepository;

    @Transactional(readOnly = true)
    public PrevisaoDTO prever(Integer meses) {
        log.info("Calculando a previsão... - Meses: {}", meses);
        var horizonte = meses != null ? meses : 6;

        if (horizonte < 1 || horizonte > 24) {
            log.warn("Informe um horizonte entre 1 e 24 meses!");
            throw new RequisicaoInvalidaException("Informe um horizonte entre 1 e 24 meses!");
        }

        var hoje = LocalDate.now();
        var mesAtual = YearMonth.from(hoje);
        var ultimoMes = mesAtual.plusMonths(horizonte);
        var inicioDaBase = mesAtual.minusMonths(MESES_DA_BASE);
        var mesesDaBase = Stream.iterate(inicioDaBase, mes -> mes.isBefore(mesAtual), mes -> mes.plusMonths(1)).toList();

        var lancamentos = lancamentoRepository.buscarComOrigemEntre(inicioDaBase.atDay(1), ultimoMes.atEndOfMonth());
        var recorrentes = despesaRecorrenteRepository.buscarComOrigemECategoria()
                .stream()
                .filter(despesa -> despesa.getSituacao() == SituacaoDespesaRecorrente.ATIVO)
                .filter(despesa -> pesaNoTotal(despesa.getConta()) || noCredito(despesa.getCartao()))
                .toList();

        var base = base(mesesDaBase, lancamentos, recorrentes);
        var cenario = new CenarioPrevisao(
                lancamentos,
                recorrentes,
                faturaService.parcelasPorVencimento(hoje),
                base,
                base.despesaMedia().subtract(base.recorrentesMedia()).max(BigDecimal.ZERO),
                despesasRepetidas(mesesDaBase, lancamentos),
                recorrentes.stream().map(despesa -> normalizar(despesa.getDescricao())).collect(Collectors.toSet()));

        var saldoAtual = dinheiro(zeroSeNulo(contaRepository.somarSaldoDoTotal()));
        var emAberto = lancamentoRepository.buscarNaoPagosAte(mesAtual.atEndOfMonth());
        var restanteMesAtual = projetarRestante(cenario, emAberto, hoje, saldoAtual);

        var saldo = restanteMesAtual.saldoFinal();
        var projecao = new ArrayList<MesPrevisaoDTO>();

        for (var mes = mesAtual.plusMonths(1); !mes.isAfter(ultimoMes); mes = mes.plusMonths(1)) {
            var projetado = projetarMes(cenario, mes, saldo);
            projecao.add(projetado);
            saldo = projetado.saldoFinal();
        }

        var menorSaldo = Stream.concat(Stream.of(restanteMesAtual), projecao.stream())
                .min(Comparator.comparing(MesPrevisaoDTO::saldoFinal))
                .map(mes -> new MenorSaldoDTO(mes.mes(), mes.saldoFinal()))
                .orElseThrow();

        var resultadoMedio = somar(projecao.stream().map(MesPrevisaoDTO::resultado))
                .divide(BigDecimal.valueOf(projecao.size()), 2, RoundingMode.HALF_UP);

        return new PrevisaoDTO(
                saldoAtual,
                restanteMesAtual,
                restanteMesAtual.saldoFinal(),
                projecao,
                saldo,
                resultadoMedio,
                menorSaldo,
                base);
    }

    private static BaseCalculoPrevisaoDTO base(List<YearMonth> mesesDaBase, List<Lancamento> lancamentos, List<DespesaRecorrente> recorrentes) {
        var inicio = mesesDaBase.getFirst().atDay(1);
        var fim = mesesDaBase.getLast().atEndOfMonth();
        var daBase = lancamentos.stream().filter(lancamento -> entre(lancamento.getData(), inicio, fim)).toList();

        var recorrentesDaBase = somar(mesesDaBase.stream()
                .map(mes -> somarRecorrentes(recorrentes, mes.atDay(1), mes.atEndOfMonth())));

        return new BaseCalculoPrevisaoDTO(
                mesesDaBase.stream().map(YearMonth::toString).toList(),
                media(somarLancamentos(daBase, PrevisaoService::receitaDoTotal)),
                media(somarLancamentos(daBase, PrevisaoService::despesaDoTotal)),
                media(recorrentesDaBase),
                media(somarLancamentos(daBase, PrevisaoService::aporte)));
    }

    private static MesPrevisaoDTO projetarRestante(CenarioPrevisao cenario, List<Lancamento> emAberto, LocalDate hoje, BigDecimal saldoAtual) {
        var mes = YearMonth.from(hoje);
        var inicio = hoje.plusDays(1);
        var fim = mes.atEndOfMonth();

        var diasRestantes = BigDecimal.valueOf(mes.lengthOfMonth() - hoje.getDayOfMonth());
        var variavel = cenario.variavel().multiply(diasRestantes).divide(BigDecimal.valueOf(mes.lengthOfMonth()), 10, RoundingMode.HALF_UP);

        return montarMes(
                mes,
                somarLancamentos(emAberto, PrevisaoService::receitaDoTotal),
                recorrentesSemLancamento(cenario, mes, inicio, fim),
                parcelasEntre(cenario, inicio, fim),
                variavel,
                somarLancamentos(emAberto, PrevisaoService::despesaDoTotal),
                somarLancamentos(emAberto, PrevisaoService::aporte),
                saldoAtual);
    }

    private static MesPrevisaoDTO projetarMes(CenarioPrevisao cenario, YearMonth mes, BigDecimal saldoInicial) {
        var inicio = mes.atDay(1);
        var fim = mes.atEndOfMonth();
        var agendados = agendadosEntre(cenario.lancamentos(), inicio, fim);

        var despesasAgendadas = agendados.stream()
                .filter(lancamento -> {
                    var descricao = normalizar(lancamento.getDescricao());
                    return !cenario.despesasRepetidasNaBase().contains(descricao) || cenario.nomesDasRecorrentes().contains(descricao);
                })
                .toList();

        return montarMes(
                mes,
                cenario.base().receitaMedia().add(somarLancamentos(agendados, PrevisaoService::receitaDoTotal)),
                recorrentesSemLancamento(cenario, mes, inicio, fim),
                parcelasEntre(cenario, inicio, fim),
                cenario.variavel(),
                somarLancamentos(despesasAgendadas, PrevisaoService::despesaDoTotal),
                cenario.base().aportesMedia().add(somarLancamentos(agendados, PrevisaoService::aporte)),
                saldoInicial);
    }

    private static MesPrevisaoDTO montarMes(YearMonth mes, BigDecimal receita, BigDecimal recorrentes, BigDecimal parcelas, BigDecimal variavel, BigDecimal agendados, BigDecimal aportes, BigDecimal saldoInicial) {
        var despesa = dinheiro(recorrentes).add(dinheiro(parcelas)).add(dinheiro(variavel)).add(dinheiro(agendados));
        var resultado = dinheiro(receita).subtract(despesa).subtract(dinheiro(aportes));

        return new MesPrevisaoDTO(
                mes,
                MesDoAno.rotulo(mes),
                dinheiro(receita),
                dinheiro(recorrentes),
                dinheiro(parcelas),
                dinheiro(variavel),
                dinheiro(agendados),
                despesa,
                dinheiro(aportes),
                resultado,
                saldoInicial.add(resultado));
    }

    private static BigDecimal recorrentesSemLancamento(CenarioPrevisao cenario, YearMonth mes, LocalDate inicio, LocalDate fim) {
        var lancadasNoMes = cenario.lancamentos().stream()
                .filter(lancamento -> lancamento.getTipo() == TipoLancamento.DESPESA)
                .filter(lancamento -> YearMonth.from(lancamento.getData()).equals(mes))
                .map(lancamento -> normalizar(lancamento.getDescricao()))
                .collect(Collectors.toSet());

        var semLancamento = cenario.recorrentes().stream()
                .filter(despesa -> !lancadasNoMes.contains(normalizar(despesa.getDescricao())))
                .toList();

        return somarRecorrentes(semLancamento, inicio, fim);
    }

    private static Set<String> despesasRepetidas(List<YearMonth> mesesDaBase, List<Lancamento> lancamentos) {
        return mesesDaBase.stream()
                .map(mes -> lancamentos.stream()
                        .filter(PrevisaoService::despesaDoTotal)
                        .filter(lancamento -> YearMonth.from(lancamento.getData()).equals(mes))
                        .map(lancamento -> normalizar(lancamento.getDescricao()))
                        .collect(Collectors.toSet()))
                .reduce((acumuladas, doMes) -> acumuladas.stream().filter(doMes::contains).collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    private static List<Lancamento> agendadosEntre(List<Lancamento> lancamentos, LocalDate inicio, LocalDate fim) {
        return lancamentos.stream()
                .filter(lancamento -> lancamento.getSituacao() != SituacaoLancamento.PAGO)
                .filter(lancamento -> entre(lancamento.getData(), inicio, fim))
                .toList();
    }

    private static BigDecimal parcelasEntre(CenarioPrevisao cenario, LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) return BigDecimal.ZERO;

        return somar(cenario.parcelas().subMap(inicio, true, fim, true).values().stream());
    }

    private static BigDecimal somarRecorrentes(Collection<DespesaRecorrente> recorrentes, LocalDate inicio, LocalDate fim) {
        return somar(recorrentes.stream()
                .map(despesa -> despesa.getValor().multiply(BigDecimal.valueOf(ocorrenciasEntre(despesa, inicio, fim)))));
    }

    private static long ocorrenciasEntre(DespesaRecorrente despesa, LocalDate inicio, LocalDate fim) {
        var frequencia = despesa.getFrequencia();
        var ocorrencias = 0L;

        for (var vencimento = frequencia.ocorrenciaAnterior(despesa.getProximoVencimento()); !vencimento.isBefore(inicio); vencimento = frequencia.ocorrenciaAnterior(vencimento)) {
            if (!vencimento.isAfter(fim)) ocorrencias++;
        }

        for (var vencimento = despesa.getProximoVencimento(); !vencimento.isAfter(fim); vencimento = frequencia.proximaOcorrencia(vencimento)) {
            if (!vencimento.isBefore(inicio)) ocorrencias++;
        }

        return ocorrencias;
    }

    private static BigDecimal somarLancamentos(List<Lancamento> lancamentos, Predicate<Lancamento> criterio) {
        return somar(lancamentos.stream()
                .filter(criterio)
                .map(Lancamento::getValor));
    }

    private static boolean receitaDoTotal(Lancamento lancamento) {
        return lancamento.getTipo() == TipoLancamento.RECEITA && pesaNoTotal(lancamento.getConta());
    }

    private static boolean despesaDoTotal(Lancamento lancamento) {
        return lancamento.getTipo() == TipoLancamento.DESPESA && (pesaNoTotal(lancamento.getConta()) || noCredito(lancamento.getCartao()));
    }

    private static boolean aporte(Lancamento lancamento) {
        return lancamento.getTipo() == TipoLancamento.TRANSFERENCIA && pesaNoTotal(lancamento.getConta()) && !pesaNoTotal(lancamento.getContaDestino());
    }

    private static boolean pesaNoTotal(Conta conta) {
        return conta != null && conta.getSituacao() == Situacao.ATIVO && Boolean.TRUE.equals(conta.getIncluirNoTotal());
    }

    private static boolean noCredito(Cartao cartao) {
        return cartao != null && cartao.getTipo() == TipoCartao.CREDITO;
    }

    private static boolean entre(LocalDate data, LocalDate inicio, LocalDate fim) {
        return !data.isBefore(inicio) && !data.isAfter(fim);
    }

    private static String normalizar(String descricao) {
        return descricao.strip().toLowerCase(Locale.ROOT);
    }

    private static BigDecimal media(BigDecimal total) {
        return dinheiro(total.divide(BigDecimal.valueOf(MESES_DA_BASE), 10, RoundingMode.HALF_UP));
    }

    private static BigDecimal somar(Stream<BigDecimal> valores) {
        return valores.reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal dinheiro(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}