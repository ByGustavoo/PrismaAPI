package br.com.prismaapi.model.dto.previsao;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.YearMonth;

@Schema(description = "Representa o mês de menor saldo projetado.")
public record MenorSaldoDTO(

        @JsonFormat(pattern = "yyyy-MM")
        YearMonth mes,

        BigDecimal saldo

) {}