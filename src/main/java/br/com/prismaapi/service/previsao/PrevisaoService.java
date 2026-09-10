package br.com.prismaapi.service.previsao;

import br.com.prismaapi.enums.Frequencia;
import br.com.prismaapi.enums.MesDoAno;
import br.com.prismaapi.enums.SituacaoDespesaRecorrente;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.exceptions.RequisicaoInvalidaException;
import br.com.prismaapi.model.dto.dashboard.projection.TotalMensalProjecao;
import br.com.prismaapi.model.dto.previsao.MenorSaldoDTO;
import br.com.prismaapi.model.dto.previsao.MesPrevisaoDTO;
import br.com.prismaapi.model.dto.previsao.PrevisaoDTO;
import br.com.prismaapi.model.entity.despesarecorrente.DespesaRecorrente;
import br.com.prismaapi.repository.compraparcelada.CompraParceladaRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.despesarecorrente.DespesaRecorrenteRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.service.fatura.FaturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PrevisaoService {

    private final FaturaService faturaService;
    private static final int MESES_DA_BASE = 3;
    private final ContaRepository contaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CompraParceladaRepository compraParceladaRepository;
    private final DespesaRecorrenteRepository despesaRecorrenteRepository;

    @Transactional(readOnly = true)
    public PrevisaoDTO prever(Integer meses) {
        var horizonte = meses != null ? meses : 6;

        if (horizonte < 1 || horizonte > 24) {
            throw new RequisicaoInvalidaException("Informe um horizonte entre 1 e 24 meses!");
        }

        var mesAtual = YearMonth.now();
        var inicioDaBase = mesAtual.minusMonths(MESES_DA_BASE);
        var fimDaBase = mesAtual.minusMonths(1);
        var ultimoMes = mesAtual.plusMonths(horizonte);

        var recorrentes = despesaRecorrenteRepository.findBySituacao(SituacaoDespesaRecorrente.ATIVO);
        var parcelas = compraParceladaRepository.buscarParcelasAte(ultimoMes.atDay(1));
        var totais = lancamentoRepository.agruparTotaisPorMes(inicioDaBase.atDay(1), fimDaBase.atEndOfMonth());
        var mesesDaBase = Stream.iterate(inicioDaBase, mes -> !mes.isAfter(fimDaBase), mes -> mes.plusMonths(1)).toList();

        var receita = dinheiro(media(somarPorTipo(totais, TipoLancamento.RECEITA)));
        var despesaMedia = media(somarPorTipo(totais, TipoLancamento.DESPESA));
        var recorrentesMedias = media(somar(mesesDaBase.stream().map(mes -> recorrentesNoMes(recorrentes, mes))));
        var variavel = dinheiro(despesaMedia.subtract(recorrentesMedias).max(BigDecimal.ZERO));

        var saldoInicial = dinheiro(zeroSeNulo(contaRepository.somarSaldoDoTotal()));
        var saldo = saldoInicial;
        var projecao = new ArrayList<MesPrevisaoDTO>();

        for (var mes = mesAtual.plusMonths(1); !mes.isAfter(ultimoMes); mes = mes.plusMonths(1)) {
            var recorrentesDoMes = dinheiro(recorrentesNoMes(recorrentes, mes));
            var parcelasDoMes = dinheiro(faturaService.parcelasNoMes(parcelas, mes));
            var despesa = recorrentesDoMes.add(parcelasDoMes).add(variavel);
            var resultado = receita.subtract(despesa);

            saldo = saldo.add(resultado);

            projecao.add(new MesPrevisaoDTO(
                    mes,
                    MesDoAno.rotulo(mes),
                    receita,
                    recorrentesDoMes,
                    parcelasDoMes,
                    variavel,
                    despesa,
                    resultado,
                    saldo));
        }

        var menorSaldo = projecao.stream()
                .min(Comparator.comparing(MesPrevisaoDTO::saldoFinal))
                .map(mes -> new MenorSaldoDTO(mes.mes(), mes.saldoFinal()))
                .orElseThrow();

        var resultadoMedio = somar(projecao.stream().map(MesPrevisaoDTO::resultado))
                .divide(BigDecimal.valueOf(projecao.size()), 2, RoundingMode.HALF_UP);

        return new PrevisaoDTO(saldoInicial, projecao, saldo, resultadoMedio, menorSaldo);
    }

    private static BigDecimal somarPorTipo(List<TotalMensalProjecao> totais, TipoLancamento tipo) {
        return somar(totais.stream()
                .filter(total -> total.tipo() == tipo)
                .map(TotalMensalProjecao::valor));
    }

    private static BigDecimal recorrentesNoMes(List<DespesaRecorrente> recorrentes, YearMonth mes) {
        return somar(recorrentes.stream()
                .map(despesa -> despesa.getValor().multiply(BigDecimal.valueOf(ocorrenciasNoMes(despesa, mes)))));
    }

    private static long ocorrenciasNoMes(DespesaRecorrente despesa, YearMonth mes) {
        var vencimento = despesa.getProximoVencimento();

        while (vencimento.isBefore(mes.atDay(1))) {
            vencimento = proximaOcorrencia(vencimento, despesa.getFrequencia());
        }

        var ocorrencias = 0L;

        while (!vencimento.isAfter(mes.atEndOfMonth())) {
            ocorrencias++;
            vencimento = proximaOcorrencia(vencimento, despesa.getFrequencia());
        }

        return ocorrencias;
    }

    private static LocalDate proximaOcorrencia(LocalDate data, Frequencia frequencia) {
        return switch (frequencia) {
            case SEMANAL -> data.plusWeeks(1);
            case QUINZENAL -> data.plusWeeks(2);
            case MENSAL -> data.plusMonths(1);
            case BIMESTRAL -> data.plusMonths(2);
            case TRIMESTRAL -> data.plusMonths(3);
            case SEMESTRAL -> data.plusMonths(6);
            case ANUAL -> data.plusYears(1);
        };
    }

    private static BigDecimal media(BigDecimal total) {
        return total.divide(BigDecimal.valueOf(MESES_DA_BASE), 10, RoundingMode.HALF_UP);
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