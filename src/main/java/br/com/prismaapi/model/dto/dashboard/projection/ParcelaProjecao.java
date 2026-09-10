package br.com.prismaapi.model.dto.dashboard.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ParcelaProjecao(

        UUID cartaoId,

        BigDecimal valorTotal,

        Short parcelas,

        LocalDate primeiroMes

) {}