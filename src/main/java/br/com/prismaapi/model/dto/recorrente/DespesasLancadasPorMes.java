package br.com.prismaapi.model.dto.recorrente;

import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.entity.recorrente.Recorrente;
import br.com.prismaapi.util.TextoUtil;

import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record DespesasLancadasPorMes(

        Map<YearMonth, Set<UUID>> recorrentes,

        Map<YearMonth, Set<String>> descricoes

) {

    public static DespesasLancadasPorMes de(Collection<Lancamento> lancamentos) {
        var recorrentes = new HashMap<YearMonth, Set<UUID>>();
        var descricoes = new HashMap<YearMonth, Set<String>>();

        lancamentos.stream()
                .filter(lancamento -> lancamento.getTipo() == TipoLancamento.DESPESA)
                .forEach(lancamento -> {
                    var mes = YearMonth.from(lancamento.getData());

                    if (lancamento.getRecorrente() != null) {
                        recorrentes.computeIfAbsent(mes, _ -> new HashSet<>()).add(lancamento.getRecorrente().getId());
                    } else {
                        descricoes.computeIfAbsent(mes, _ -> new HashSet<>()).add(TextoUtil.normalizar(lancamento.getDescricao()));
                    }
                });

        return new DespesasLancadasPorMes(recorrentes, descricoes);
    }

    public boolean contem(Recorrente recorrente, YearMonth mes) {
        return recorrentes.getOrDefault(mes, Set.of()).contains(recorrente.getId())
                || descricoes.getOrDefault(mes, Set.of()).contains(TextoUtil.normalizar(recorrente.getDescricao()));
    }
}