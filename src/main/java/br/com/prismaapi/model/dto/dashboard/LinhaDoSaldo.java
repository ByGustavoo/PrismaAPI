package br.com.prismaapi.model.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;

public record LinhaDoSaldo(

        LocalDate hoje,

        BigDecimal saldoDeHoje,

        NavigableMap<LocalDate, BigDecimal> movimentos

) {

    public BigDecimal em(LocalDate data) {
        if (data.isEqual(hoje)) {
            return saldoDeHoje;
        }

        if (data.isAfter(hoje)) {
            return saldoDeHoje.add(somar(movimentos.subMap(hoje, false, data, true)));
        }

        return saldoDeHoje.subtract(somar(movimentos.subMap(data, false, hoje, true)));
    }

    private static BigDecimal somar(Map<LocalDate, BigDecimal> trecho) {
        return trecho.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}