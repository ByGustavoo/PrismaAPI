package br.com.prismaapi.model.dto.dashboard.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ValorPorDataProjecao(

        LocalDate data,

        BigDecimal valor

) {}