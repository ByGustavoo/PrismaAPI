package br.com.prismaapi.model.dto.previsao;

import br.com.prismaapi.model.dto.recorrente.DespesasLancadasPorMes;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.entity.recorrente.Recorrente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

public record CenarioPrevisao(

        Map<YearMonth, List<Lancamento>> agendadosPorMes,

        DespesasLancadasPorMes despesasLancadas,

        List<Recorrente> recorrentes,

        NavigableMap<LocalDate, BigDecimal> parcelas,

        BaseCalculoPrevisaoDTO base,

        BigDecimal variavel,

        Set<String> despesasRepetidasNaBase,

        Set<String> nomesDasRecorrentes

) {}