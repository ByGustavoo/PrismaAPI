package br.com.prismaapi.model.dto.dashboard;

import br.com.prismaapi.util.ValorUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
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
            return saldoDeHoje.add(ValorUtil.somar(movimentos.subMap(hoje, false, data, true).values().stream()));
        }

        return saldoDeHoje.subtract(ValorUtil.somar(movimentos.subMap(data, false, hoje, true).values().stream()));
    }
}