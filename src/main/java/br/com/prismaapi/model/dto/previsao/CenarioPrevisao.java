package br.com.prismaapi.model.dto.previsao;

import br.com.prismaapi.model.entity.despesarecorrente.DespesaRecorrente;
import br.com.prismaapi.model.entity.lancamento.Lancamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;

public record CenarioPrevisao(

        List<Lancamento> lancamentos,

        List<DespesaRecorrente> recorrentes,

        NavigableMap<LocalDate, BigDecimal> parcelas,

        BaseCalculoPrevisaoDTO base,

        BigDecimal variavel,

        Set<String> despesasRepetidasNaBase,

        Set<String> nomesDasRecorrentes

) {}