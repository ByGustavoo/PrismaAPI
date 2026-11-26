package br.com.prismaapi.service.relatorio;

import br.com.prismaapi.enums.GrupoOrigem;
import br.com.prismaapi.enums.MesDoAno;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.exceptions.RequisicaoInvalidaException;
import br.com.prismaapi.model.dto.dashboard.LinhaDoSaldo;
import br.com.prismaapi.model.dto.dashboard.fluxo.FluxoDTO;
import br.com.prismaapi.model.dto.dashboard.gasto.categoria.GastoCategoriaDTO;
import br.com.prismaapi.model.dto.dashboard.projection.MovimentoDiarioProjecao;
import br.com.prismaapi.model.dto.dashboard.saldo.SaldoDTO;
import br.com.prismaapi.model.dto.dashboard.variacao.VariacaoDTO;
import br.com.prismaapi.model.dto.relatorio.BaldeRelatorio;
import br.com.prismaapi.model.dto.relatorio.GastoOrigemDTO;
import br.com.prismaapi.model.dto.relatorio.PatrimonioDTO;
import br.com.prismaapi.model.dto.relatorio.RelatorioDTO;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.service.investimento.InvestimentoService;
import br.com.prismaapi.service.saldo.SaldoService;
import br.com.prismaapi.util.ValorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final SaldoService saldoService;
    private final CategoriaMapper categoriaMapper;
    private final InvestimentoService investimentoService;
    private final LancamentoRepository lancamentoRepository;
    private static final DateTimeFormatter FORMATO_DO_DIA = DateTimeFormatter.ofPattern("dd/MM");

    @Cacheable("relatorios")
    @Transactional(readOnly = true)
    public RelatorioDTO resumir(LocalDate dataInicial, LocalDate dataFinal) {
        log.info("Resumindo o relatório... - Data Inicial: {} - Data Final: {}", dataInicial, dataFinal);
        validar(dataInicial, dataFinal);

        var hoje = LocalDate.now();
        var dias = ChronoUnit.DAYS.between(dataInicial, dataFinal) + 1;
        var fimAnterior = dataInicial.minusDays(1);
        var inicioAnterior = fimAnterior.minusDays(dias - 1);

        var resumo = lancamentoRepository.resumirPeriodos(dataInicial, dataFinal, inicioAnterior, fimAnterior);
        var receitas = ValorUtil.dinheiro(resumo.receitas());
        var despesas = ValorUtil.dinheiro(resumo.despesas());

        var baldes = baldes(dataInicial, dataFinal, dias);
        var mesesDoPatrimonio = mesesDoPatrimonio(dataInicial, dataFinal);

        var datasDoSaldo = new ArrayList<LocalDate>();
        baldes.forEach(balde -> datasDoSaldo.add(balde.fim()));
        mesesDoPatrimonio.forEach(mes -> datasDoSaldo.add(fechamento(mes, hoje)));

        var linhaDoSaldo = saldoService.linhaDoSaldo(datasDoSaldo, hoje);

        return new RelatorioDTO(
                dataInicial,
                dataFinal,
                receitas,
                despesas,
                receitas.subtract(despesas),
                VariacaoDTO.entre(receitas, ValorUtil.dinheiro(resumo.receitasAnteriores())),
                VariacaoDTO.entre(despesas, ValorUtil.dinheiro(resumo.despesasAnteriores())),
                resumo.quantidade().intValue(),
                porCategoria(TipoLancamento.DESPESA, dataInicial, dataFinal, despesas),
                porCategoria(TipoLancamento.RECEITA, dataInicial, dataFinal, receitas),
                fluxoCaixa(baldes, dataInicial, dataFinal),
                despesasPorOrigem(dataInicial, dataFinal, despesas),
                baldes.stream().map(balde -> new SaldoDTO(balde.rotulo(), ValorUtil.dinheiro(linhaDoSaldo.em(balde.fim())))).toList(),
                patrimonio(mesesDoPatrimonio, linhaDoSaldo, hoje));
    }

    private static void validar(LocalDate dataInicial, LocalDate dataFinal) {
        if (dataInicial == null || dataFinal == null) {
            log.warn("Informe o início e o fim do período!");
            throw new RequisicaoInvalidaException("Informe o início e o fim do período!");
        }

        if (dataInicial.isAfter(dataFinal)) {
            log.warn("O período informado é inválido!");
            throw new RequisicaoInvalidaException("O período informado é inválido!");
        }
    }

    private static List<BaldeRelatorio> baldes(LocalDate inicio, LocalDate fim, long dias) {
        var baldes = new ArrayList<BaldeRelatorio>();

        if (dias <= 10) {
            for (var dia = inicio; !dia.isAfter(fim); dia = dia.plusDays(1)) {
                baldes.add(new BaldeRelatorio(dia.format(FORMATO_DO_DIA), dia, dia));
            }

            return baldes;
        }

        if (dias <= 45) {
            for (var comeco = inicio; !comeco.isAfter(fim); comeco = comeco.plusWeeks(1)) {
                var termino = comeco.plusDays(6);
                baldes.add(new BaldeRelatorio(comeco.format(FORMATO_DO_DIA), comeco, termino.isAfter(fim) ? fim : termino));
            }

            return baldes;
        }

        for (var mes = YearMonth.from(inicio); !mes.isAfter(YearMonth.from(fim)); mes = mes.plusMonths(1)) {
            var comeco = mes.atDay(1).isBefore(inicio) ? inicio : mes.atDay(1);
            var termino = mes.atEndOfMonth().isAfter(fim) ? fim : mes.atEndOfMonth();
            baldes.add(new BaldeRelatorio(MesDoAno.rotulo(mes), comeco, termino));
        }

        return baldes;
    }

    private static List<YearMonth> mesesDoPatrimonio(LocalDate inicio, LocalDate fim) {
        var ultimoMes = YearMonth.from(fim);
        var quantidade = Math.max(YearMonth.from(inicio).until(ultimoMes, ChronoUnit.MONTHS) + 1, 6);

        return Stream.iterate(ultimoMes.minusMonths(quantidade - 1), mes -> mes.plusMonths(1))
                .limit(quantidade)
                .toList();
    }

    private List<GastoCategoriaDTO> porCategoria(TipoLancamento tipo, LocalDate inicio, LocalDate fim, BigDecimal total) {
        return lancamentoRepository.agruparPorCategoria(tipo, inicio, fim)
                .stream()
                .map(grupo -> new GastoCategoriaDTO(
                        categoriaMapper.toDTO(grupo.categoria()),
                        ValorUtil.dinheiro(grupo.valor()),
                        ValorUtil.fracao(grupo.valor(), total)))
                .toList();
    }

    private List<FluxoDTO> fluxoCaixa(List<BaldeRelatorio> baldes, LocalDate inicio, LocalDate fim) {
        var movimentos = lancamentoRepository.agruparReceitasEDespesasPorDia(inicio, fim);

        return baldes.stream()
                .map(balde -> new FluxoDTO(
                        balde.rotulo(),
                        somarNoBalde(movimentos, balde, TipoLancamento.RECEITA),
                        somarNoBalde(movimentos, balde, TipoLancamento.DESPESA)))
                .toList();
    }

    private List<GastoOrigemDTO> despesasPorOrigem(LocalDate inicio, LocalDate fim, BigDecimal total) {
        return lancamentoRepository.agruparDespesasPorOrigem(inicio, fim)
                .stream()
                .map(gasto -> gasto.idConta() != null
                        ? new GastoOrigemDTO(gasto.idConta(), gasto.nomeConta(), GrupoOrigem.CONTA, ValorUtil.dinheiro(gasto.valor()), ValorUtil.fracao(gasto.valor(), total))
                        : new GastoOrigemDTO(gasto.idCartao(), gasto.nomeCartao(), GrupoOrigem.CARTAO, ValorUtil.dinheiro(gasto.valor()), ValorUtil.fracao(gasto.valor(), total)))
                .toList();
    }

    private List<PatrimonioDTO> patrimonio(List<YearMonth> meses, LinhaDoSaldo linhaDoSaldo, LocalDate hoje) {
        return investimentoService.evolucaoNosMeses(meses)
                .stream()
                .map(carteira -> {
                    var contas = ValorUtil.dinheiro(linhaDoSaldo.em(fechamento(carteira.mes(), hoje)));
                    return new PatrimonioDTO(carteira.mes(), carteira.rotulo(), contas, carteira.valor(), contas.add(carteira.valor()));
                })
                .toList();
    }

    private static BigDecimal somarNoBalde(List<MovimentoDiarioProjecao> movimentos, BaldeRelatorio balde, TipoLancamento tipo) {
        return ValorUtil.dinheiro(ValorUtil.somar(movimentos.stream()
                .filter(movimento -> movimento.tipo() == tipo)
                .filter(movimento -> balde.contem(movimento.data()))
                .map(MovimentoDiarioProjecao::valor)));
    }

    private static LocalDate fechamento(YearMonth mes, LocalDate hoje) {
        return mes.equals(YearMonth.from(hoje)) ? hoje : mes.atEndOfMonth();
    }
}