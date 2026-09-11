package br.com.prismaapi.service.relatorio;

import br.com.prismaapi.enums.GrupoOrigem;
import br.com.prismaapi.enums.MesDoAno;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.exceptions.RequisicaoInvalidaException;
import br.com.prismaapi.model.dto.dashboard.LinhaDoSaldo;
import br.com.prismaapi.model.dto.dashboard.fluxo.FluxoDTO;
import br.com.prismaapi.model.dto.dashboard.gasto.categoria.GastoCategoriaDTO;
import br.com.prismaapi.model.dto.dashboard.projection.GastoCategoriaProjecao;
import br.com.prismaapi.model.dto.dashboard.projection.MovimentoDiarioProjecao;
import br.com.prismaapi.model.dto.dashboard.saldo.SaldoDTO;
import br.com.prismaapi.model.dto.dashboard.variacao.VariacaoDTO;
import br.com.prismaapi.model.dto.relatorio.BaldeRelatorio;
import br.com.prismaapi.model.dto.relatorio.GastoOrigemDTO;
import br.com.prismaapi.model.dto.relatorio.PatrimonioDTO;
import br.com.prismaapi.model.dto.relatorio.RelatorioDTO;
import br.com.prismaapi.model.entity.categoria.Categoria;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.service.investimento.InvestimentoService;
import br.com.prismaapi.service.saldo.SaldoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final SaldoService saldoService;
    private static final int CASAS_DA_FRACAO = 4;
    private final CategoriaMapper categoriaMapper;
    private final CategoriaRepository categoriaRepository;
    private final InvestimentoService investimentoService;
    private final LancamentoRepository lancamentoRepository;
    private static final DateTimeFormatter FORMATO_DO_DIA = DateTimeFormatter.ofPattern("dd/MM");

    @Transactional(readOnly = true)
    public RelatorioDTO resumir(LocalDate dataInicial, LocalDate dataFinal) {
        log.info("Resumindo o relatório... - Data Inicial: {} - Data Final: {}", dataInicial, dataFinal);
        validar(dataInicial, dataFinal);

        var hoje = LocalDate.now();
        var dias = ChronoUnit.DAYS.between(dataInicial, dataFinal) + 1;
        var fimAnterior = dataInicial.minusDays(1);
        var inicioAnterior = fimAnterior.minusDays(dias - 1);

        var receitas = somarPorTipo(TipoLancamento.RECEITA, dataInicial, dataFinal);
        var despesas = somarPorTipo(TipoLancamento.DESPESA, dataInicial, dataFinal);

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
                VariacaoDTO.entre(receitas, somarPorTipo(TipoLancamento.RECEITA, inicioAnterior, fimAnterior)),
                VariacaoDTO.entre(despesas, somarPorTipo(TipoLancamento.DESPESA, inicioAnterior, fimAnterior)),
                (int) lancamentoRepository.countByTipoNotAndDataBetween(TipoLancamento.TRANSFERENCIA, dataInicial, dataFinal),
                porCategoria(TipoLancamento.DESPESA, dataInicial, dataFinal, despesas),
                porCategoria(TipoLancamento.RECEITA, dataInicial, dataFinal, receitas),
                fluxoCaixa(baldes, dataInicial, dataFinal),
                despesasPorOrigem(dataInicial, dataFinal, despesas),
                baldes.stream().map(balde -> new SaldoDTO(balde.rotulo(), dinheiro(linhaDoSaldo.em(balde.fim())))).toList(),
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

    private BigDecimal somarPorTipo(TipoLancamento tipo, LocalDate inicio, LocalDate fim) {
        var total = lancamentoRepository.somarPorTipo(tipo, inicio, fim);
        return dinheiro(total != null ? total : BigDecimal.ZERO);
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
        var grupos = lancamentoRepository.agruparPorCategoria(tipo, inicio, fim);

        if (grupos.isEmpty()) {
            return List.of();
        }

        var categorias = categoriaRepository.findAllById(grupos.stream().map(GastoCategoriaProjecao::categoriaId).toList())
                .stream()
                .collect(Collectors.toMap(Categoria::getId, Function.identity()));

        return grupos.stream()
                .map(grupo -> new GastoCategoriaDTO(
                        categoriaMapper.toDTO(categorias.get(grupo.categoriaId())),
                        dinheiro(grupo.valor()),
                        fracao(grupo.valor(), total)))
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
                        ? new GastoOrigemDTO(gasto.idConta(), gasto.nomeConta(), GrupoOrigem.CONTA, dinheiro(gasto.valor()), fracao(gasto.valor(), total))
                        : new GastoOrigemDTO(gasto.idCartao(), gasto.nomeCartao(), GrupoOrigem.CARTAO, dinheiro(gasto.valor()), fracao(gasto.valor(), total)))
                .toList();
    }

    private List<PatrimonioDTO> patrimonio(List<YearMonth> meses, LinhaDoSaldo linhaDoSaldo, LocalDate hoje) {
        return investimentoService.evolucaoNosMeses(meses)
                .stream()
                .map(carteira -> {
                    var contas = dinheiro(linhaDoSaldo.em(fechamento(carteira.mes(), hoje)));
                    return new PatrimonioDTO(carteira.mes(), carteira.rotulo(), contas, carteira.valor(), contas.add(carteira.valor()));
                })
                .toList();
    }

    private static BigDecimal somarNoBalde(List<MovimentoDiarioProjecao> movimentos, BaldeRelatorio balde, TipoLancamento tipo) {
        return dinheiro(movimentos.stream()
                .filter(movimento -> movimento.tipo() == tipo)
                .filter(movimento -> balde.contem(movimento.data()))
                .map(MovimentoDiarioProjecao::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static LocalDate fechamento(YearMonth mes, LocalDate hoje) {
        return mes.equals(YearMonth.from(hoje)) ? hoje : mes.atEndOfMonth();
    }

    private static BigDecimal fracao(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO.setScale(CASAS_DA_FRACAO, RoundingMode.HALF_UP);
        }

        return parte.divide(total, CASAS_DA_FRACAO, RoundingMode.HALF_UP);
    }

    private static BigDecimal dinheiro(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}