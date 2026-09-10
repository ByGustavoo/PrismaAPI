package br.com.prismaapi.service.dashboard;

import br.com.prismaapi.enums.Tendencia;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.exceptions.RequisicaoInvalidaException;
import br.com.prismaapi.model.dto.dashboard.DashboardDTO;
import br.com.prismaapi.model.dto.dashboard.LinhaDoSaldo;
import br.com.prismaapi.model.dto.dashboard.PeriodoDashboard;
import br.com.prismaapi.model.dto.dashboard.fluxo.FluxoDTO;
import br.com.prismaapi.model.dto.dashboard.gasto.categoria.GastoCategoriaDTO;
import br.com.prismaapi.model.dto.dashboard.gasto.diario.GastoDiarioDTO;
import br.com.prismaapi.model.dto.dashboard.projection.*;
import br.com.prismaapi.model.dto.dashboard.saldo.SaldoDTO;
import br.com.prismaapi.model.dto.dashboard.variacao.VariacaoDTO;
import br.com.prismaapi.model.dto.lancamento.LancamentoDTO;
import br.com.prismaapi.model.entity.categoria.Categoria;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import br.com.prismaapi.model.mapper.lancamento.LancamentoMapper;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.investimento.InvestimentoRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.service.fatura.FaturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final String[] ROTULOS_DOS_MESES = {
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
            "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    };

    private static final int CASAS_DO_PERCENTUAL = 1;
    private static final int CASAS_DA_PARTICIPACAO = 4;

    private final FaturaService faturaService;
    private final ContaRepository contaRepository;
    private final CategoriaMapper categoriaMapper;
    private final LancamentoMapper lancamentoMapper;
    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final InvestimentoRepository investimentoRepository;

    private static final String PERIODO_INVALIDO = "O período informado é inválido!";
    private static final BigDecimal FAIXA_DA_ESTABILIDADE = new BigDecimal("0.05");

    @Transactional(readOnly = true)
    public DashboardDTO resumir(YearMonth dataInicial, YearMonth dataFinal) {
        validar(dataInicial, dataFinal);

        var periodo = PeriodoDashboard.resolver(dataInicial, dataFinal, LocalDate.now());
        var anterior = periodo.anterior();
        var linhaDoSaldo = montarLinhaDoSaldo(periodo, anterior);

        var saldoAtual = linhaDoSaldo.em(periodo.dataDeCorte());
        var saldoAnterior = linhaDoSaldo.em(anterior.dataDeCorte());

        var receitas = somar(TipoLancamento.RECEITA, periodo);
        var despesas = somar(TipoLancamento.DESPESA, periodo);
        var receitasAnteriores = somar(TipoLancamento.RECEITA, anterior);
        var despesasAnteriores = somar(TipoLancamento.DESPESA, anterior);

        var carteira = investimentoRepository.resumirCarteira();

        return new DashboardDTO(
                periodo.dataInicial().toString(),
                periodo.dataFinal().toString(),
                dinheiro(saldoAtual),
                variacao(saldoAtual, saldoAnterior),
                dinheiro(receitas),
                variacao(receitas, receitasAnteriores),
                dinheiro(despesas),
                variacao(despesas, despesasAnteriores),
                dinheiro(totalInvestido(carteira)),
                rentabilidade(carteira),
                faturaService.faturaEmDestaque(periodo.dataFinal(), periodo.hoje()),
                historicoSaldo(periodo, linhaDoSaldo),
                fluxoCaixa(periodo),
                gastoDiario(periodo),
                gastoPorCategoria(periodo, despesas),
                lancamentosRecentes(periodo));
    }

    private static void validar(YearMonth dataInicial, YearMonth dataFinal) {
        if ((dataInicial == null) != (dataFinal == null)) {
            throw new RequisicaoInvalidaException(PERIODO_INVALIDO);
        }

        if (dataInicial != null && dataInicial.isAfter(dataFinal)) {
            throw new RequisicaoInvalidaException(PERIODO_INVALIDO);
        }
    }

    private LinhaDoSaldo montarLinhaDoSaldo(PeriodoDashboard periodo, PeriodoDashboard anterior) {
        var datasDeCorte = new ArrayList<LocalDate>();
        datasDeCorte.add(periodo.hoje());
        datasDeCorte.add(periodo.dataDeCorte());
        datasDeCorte.add(anterior.dataDeCorte());
        periodo.mesesDaJanela().forEach(mes -> datasDeCorte.add(periodo.dataDeCorte(mes)));

        var inicio = Collections.min(datasDeCorte);
        var fim = Collections.max(datasDeCorte);

        var movimentos = new TreeMap<LocalDate, BigDecimal>();

        lancamentoRepository.agruparMovimentoDoTotalPorDia(inicio, fim)
                .forEach(movimento -> movimentos.merge(movimento.data(), comSinal(movimento), BigDecimal::add));

        lancamentoRepository.agruparTransferenciasQueSaemDoTotal(inicio, fim)
                .forEach(saida -> movimentos.merge(saida.data(), saida.valor().negate(), BigDecimal::add));

        lancamentoRepository.agruparTransferenciasQueEntramNoTotal(inicio, fim)
                .forEach(entrada -> movimentos.merge(entrada.data(), entrada.valor(), BigDecimal::add));

        return new LinhaDoSaldo(periodo.hoje(), zeroSeNulo(contaRepository.somarSaldoDoTotal()), movimentos);
    }

    private static BigDecimal comSinal(MovimentoDiarioProjecao movimento) {
        return movimento.tipo() == TipoLancamento.RECEITA ? movimento.valor() : movimento.valor().negate();
    }

    private List<SaldoDTO> historicoSaldo(PeriodoDashboard periodo, LinhaDoSaldo linhaDoSaldo) {
        return periodo.mesesDaJanela().stream()
                .map(mes -> new SaldoDTO(rotulo(mes), dinheiro(linhaDoSaldo.em(periodo.dataDeCorte(mes)))))
                .toList();
    }

    private List<FluxoDTO> fluxoCaixa(PeriodoDashboard periodo) {
        var totais = lancamentoRepository
                .agruparTotaisPorMes(periodo.primeiroDiaDaJanela(), periodo.ultimoDiaDaJanela())
                .stream()
                .collect(Collectors.groupingBy(
                        total -> YearMonth.of(total.ano(), total.mes()),
                        Collectors.toMap(TotalMensalProjecao::tipo, TotalMensalProjecao::valor)));

        return periodo.mesesDaJanela().stream()
                .map(mes -> {
                    var doMes = totais.getOrDefault(mes, Map.of());
                    return new FluxoDTO(
                            rotulo(mes),
                            dinheiro(doMes.get(TipoLancamento.RECEITA)),
                            dinheiro(doMes.get(TipoLancamento.DESPESA)));
                })
                .toList();
    }

    private List<GastoDiarioDTO> gastoDiario(PeriodoDashboard periodo) {
        var inicio = periodo.primeiroDiaDaJanela();
        var fim = periodo.ultimoDiaDaJanela();

        var gastos = lancamentoRepository.agruparDespesasPorDia(inicio, fim).stream()
                .collect(Collectors.toMap(ValorPorDataProjecao::data, ValorPorDataProjecao::valor));

        var serie = new ArrayList<GastoDiarioDTO>();
        for (var dia = inicio; !dia.isAfter(fim); dia = dia.plusDays(1)) {
            serie.add(new GastoDiarioDTO(dia.toString(), dinheiro(gastos.get(dia))));
        }
        return serie;
    }

    private List<GastoCategoriaDTO> gastoPorCategoria(PeriodoDashboard periodo, BigDecimal totalDeDespesas) {
        var gastos = lancamentoRepository.agruparDespesasPorCategoria(periodo.primeiroDia(), periodo.ultimoDia());

        if (gastos.isEmpty()) {
            return List.of();
        }

        var categorias = categoriaRepository.findAllById(gastos.stream().map(GastoCategoriaProjecao::categoriaId).toList())
                .stream()
                .collect(Collectors.toMap(Categoria::getId, Function.identity()));

        return gastos.stream()
                .filter(gasto -> categorias.containsKey(gasto.categoriaId()))
                .map(gasto -> new GastoCategoriaDTO(
                        categoriaMapper.toDTO(categorias.get(gasto.categoriaId())),
                        dinheiro(gasto.valor()),
                        participacao(gasto.valor(), totalDeDespesas)))
                .toList();
    }

    private List<LancamentoDTO> lancamentosRecentes(PeriodoDashboard periodo) {
        return lancamentoRepository
                .buscarRecentes(periodo.primeiroDia(), periodo.ultimoDia(), PageRequest.ofSize(6))
                .stream()
                .map(lancamentoMapper::toDTO)
                .toList();
    }

    private BigDecimal somar(TipoLancamento tipo, PeriodoDashboard periodo) {
        return zeroSeNulo(lancamentoRepository.somarPorTipo(tipo, periodo.primeiroDia(), periodo.ultimoDia()));
    }

    private static BigDecimal totalInvestido(CarteiraProjecao carteira) {
        return carteira == null ? BigDecimal.ZERO : zeroSeNulo(carteira.valorAtual());
    }

    private static VariacaoDTO rentabilidade(CarteiraProjecao carteira) {
        if (carteira == null) {
            return estavel();
        }

        var aportado = zeroSeNulo(carteira.aportado());
        return variacao(zeroSeNulo(carteira.valorAtual()), aportado);
    }

    private static VariacaoDTO variacao(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.signum() == 0) {
            return estavel();
        }

        var percentual = atual.subtract(anterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(anterior.abs(), CASAS_DO_PERCENTUAL + 2, RoundingMode.HALF_UP);

        var tendencia = tendencia(percentual);
        var arredondado = percentual.setScale(CASAS_DO_PERCENTUAL, RoundingMode.HALF_UP);

        return new VariacaoDTO(tendencia == Tendencia.ESTAVEL ? zero(CASAS_DO_PERCENTUAL) : arredondado, tendencia);
    }

    private static Tendencia tendencia(BigDecimal percentual) {
        if (percentual.abs().compareTo(FAIXA_DA_ESTABILIDADE) <= 0) {
            return Tendencia.ESTAVEL;
        }
        return percentual.signum() > 0 ? Tendencia.ALTA : Tendencia.BAIXA;
    }

    private static VariacaoDTO estavel() {
        return new VariacaoDTO(zero(CASAS_DO_PERCENTUAL), Tendencia.ESTAVEL);
    }

    private static BigDecimal participacao(BigDecimal valor, BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return zero(CASAS_DA_PARTICIPACAO);
        }
        return valor.divide(total, CASAS_DA_PARTICIPACAO, RoundingMode.HALF_UP);
    }

    private static String rotulo(YearMonth mes) {
        return ROTULOS_DOS_MESES[mes.getMonthValue() - 1];
    }

    private static BigDecimal dinheiro(BigDecimal valor) {
        return zeroSeNulo(valor).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private static BigDecimal zero(int casas) {
        return BigDecimal.ZERO.setScale(casas, RoundingMode.HALF_UP);
    }
}