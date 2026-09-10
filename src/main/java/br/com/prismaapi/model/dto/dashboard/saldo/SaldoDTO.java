package br.com.prismaapi.model.dto.dashboard.saldo;

import java.math.BigDecimal;

public record SaldoDTO(

        String rotulo,

        BigDecimal saldo

) {}