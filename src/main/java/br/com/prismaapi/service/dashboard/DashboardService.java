package br.com.prismaapi.service.dashboard;

import br.com.prismaapi.enums.MesDoAno;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.exceptions.RequisicaoInvalidaException;
import br.com.prismaapi.model.dto.dashboard.DashboardDTO;
import br.com.prismaapi.model.dto.dashboard.LinhaDoSaldo;
import br.com.prismaapi.model.dto.dashboard.PeriodoDashboard;
import br.com.prismaapi.model.dto.dashboard.fluxo.FluxoDTO;
import br.com.prismaapi.model.dto.dashboard.gasto.categoria.GastoCategoriaDTO;
import br.com.prismaapi.model.dto.dashboard.gasto.diario.GastoDiarioDTO;
import br.com.prismaapi.model.dto.dashboard.projection.CarteiraProjecao;
import br.com.prismaapi.model.dto.dashboard.projection.TotalMensalProjecao;
import br.com.prismaapi.model.dto.dashboard.projection.ValorPorDataProjecao;
import br.com.prismaapi.model.dto.dashboard.saldo.SaldoDTO;
import br.com.prismaapi.model.dto.dashboard.variacao.VariacaoDTO;
import br.com.prismaapi.model.dto.lancamento.LancamentoDTO;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import br.com.prismaapi.model.mapper.lancamento.LancamentoMapper;
import br.com.prismaapi.repository.investimento.InvestimentoRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.service.fatura.FaturaService;
import br.com.prismaapi.service.investimento.InvestimentoService;
import br.com.prismaapi.service.saldo.SaldoService;
import br.com.prismaapi.util.ValorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SaldoService saldoService;
    private final FaturaService faturaService;
    private final CategoriaMapper categoriaMapper;
    private final LancamentoMapper lancamentoMapper;
    private final InvestimentoService investimentoService;
    private final LancamentoRepository lancamentoRepository;
    private final InvestimentoRepository investimentoRepository;
    private static final String PERIODO_INVALIDO = "O período informado é inválido!";

    @Cacheable("dashboard")
    @Transactional(readOnly = true)
    public DashboardDTO resumir(YearMonth dataInicial, YearMonth dataFinal) {
        log.info("Resumindo o dashboard... - Data Inicial: {} - Data Final: {}", dataInicial, dataFinal);
        validar(dataInicial, dataFinal);

        var periodo = PeriodoDashboard.resolver(dataInicial, dataFinal, LocalDate.now());
        var anterior = periodo.anterior();
        var linhaDoSaldo = montarLinhaDoSaldo(periodo, anterior);

        var saldoAtual = linhaDoSaldo.em(periodo.dataDeCorte());
        var saldoAnterior = linhaDoSaldo.em(anterior.dataDeCorte());

        var resumo = lancamentoRepository.resumirPeriodos(periodo.primeiroDia(), periodo.ultimoDia(), anterior.primeiroDia(), anterior.ultimoDia());
        var receitas = ValorUtil.zeroSeNulo(resumo.receitas());
        var despesas = ValorUtil.zeroSeNulo(resumo.despesas());
        var receitasAnteriores = ValorUtil.zeroSeNulo(resumo.receitasAnteriores());
        var despesasAnteriores = ValorUtil.zeroSeNulo(resumo.despesasAnteriores());

        var carteira = investimentoRepository.resumirCarteira();
        var carteiraNosCortes = investimentoService.evolucaoNosMeses(List.of(YearMonth.from(periodo.dataDeCorte()), YearMonth.from(anterior.dataDeCorte())));
        var patrimonio = saldoAtual.add(carteiraNosCortes.getFirst().valor());
        var patrimonioAnterior = saldoAnterior.add(carteiraNosCortes.getLast().valor());

        return new DashboardDTO(
                periodo.dataInicial().toString(),
                periodo.dataFinal().toString(),
                ValorUtil.dinheiro(saldoAtual),
                VariacaoDTO.entre(saldoAtual, saldoAnterior),
                ValorUtil.dinheiro(receitas),
                VariacaoDTO.entre(receitas, receitasAnteriores),
                ValorUtil.dinheiro(despesas),
                VariacaoDTO.entre(despesas, despesasAnteriores),
                ValorUtil.dinheiro(totalInvestido(carteira)),
                rentabilidade(carteira),
                ValorUtil.dinheiro(patrimonio),
                VariacaoDTO.entre(patrimonio, patrimonioAnterior),
                faturaService.faturaEmDestaque(periodo.dataFinal(), periodo.hoje()),
                historicoSaldo(periodo, linhaDoSaldo),
                fluxoCaixa(periodo),
                gastoDiario(periodo),
                gastoPorCategoria(periodo, despesas),
                lancamentosRecentes(periodo));
    }

    private static void validar(YearMonth dataInicial, YearMonth dataFinal) {
        if ((dataInicial == null) != (dataFinal == null)) {
            log.warn(PERIODO_INVALIDO);
            throw new RequisicaoInvalidaException(PERIODO_INVALIDO);
        }

        if (dataInicial != null && dataInicial.isAfter(dataFinal)) {
            log.warn(PERIODO_INVALIDO);
            throw new RequisicaoInvalidaException(PERIODO_INVALIDO);
        }
    }

    private LinhaDoSaldo montarLinhaDoSaldo(PeriodoDashboard periodo, PeriodoDashboard anterior) {
        var datasDeCorte = new ArrayList<LocalDate>();
        datasDeCorte.add(periodo.dataDeCorte());
        datasDeCorte.add(anterior.dataDeCorte());
        periodo.mesesDaJanela().forEach(mes -> datasDeCorte.add(periodo.dataDeCorte(mes)));

        return saldoService.linhaDoSaldo(datasDeCorte, periodo.hoje());
    }

    private List<SaldoDTO> historicoSaldo(PeriodoDashboard periodo, LinhaDoSaldo linhaDoSaldo) {
        return periodo.mesesDaJanela().stream()
                .map(mes -> new SaldoDTO(MesDoAno.rotulo(mes), ValorUtil.dinheiro(linhaDoSaldo.em(periodo.dataDeCorte(mes)))))
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
                            MesDoAno.rotulo(mes),
                            ValorUtil.dinheiro(doMes.get(TipoLancamento.RECEITA)),
                            ValorUtil.dinheiro(doMes.get(TipoLancamento.DESPESA)));
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
            serie.add(new GastoDiarioDTO(dia.toString(), ValorUtil.dinheiro(gastos.get(dia))));
        }
        return serie;
    }

    private List<GastoCategoriaDTO> gastoPorCategoria(PeriodoDashboard periodo, BigDecimal totalDeDespesas) {
        return lancamentoRepository.agruparPorCategoria(TipoLancamento.DESPESA, periodo.primeiroDia(), periodo.ultimoDia())
                .stream()
                .map(gasto -> new GastoCategoriaDTO(
                        categoriaMapper.toDTO(gasto.categoria()),
                        ValorUtil.dinheiro(gasto.valor()),
                        ValorUtil.fracao(gasto.valor(), totalDeDespesas)))
                .toList();
    }

    private List<LancamentoDTO> lancamentosRecentes(PeriodoDashboard periodo) {
        return lancamentoRepository
                .buscarRecentes(periodo.primeiroDia(), periodo.ultimoDia(), PageRequest.ofSize(6))
                .stream()
                .map(lancamentoMapper::toDTO)
                .toList();
    }

    private static BigDecimal totalInvestido(CarteiraProjecao carteira) {
        return carteira == null ? BigDecimal.ZERO : ValorUtil.zeroSeNulo(carteira.valorAtual());
    }

    private static VariacaoDTO rentabilidade(CarteiraProjecao carteira) {
        if (carteira == null) {
            return VariacaoDTO.estavel();
        }

        return VariacaoDTO.entre(ValorUtil.zeroSeNulo(carteira.valorAtual()), ValorUtil.zeroSeNulo(carteira.aportado()));
    }
}