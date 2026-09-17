package br.com.prismaapi.model.dto.previsao;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.YearMonth;

@Schema(description = "Representa um mês projetado da previsão financeira.")
public record MesPrevisaoDTO(

        @JsonFormat(pattern = "yyyy-MM")
        YearMonth mes,

        String rotulo,

        BigDecimal receita,

        BigDecimal recorrentes,

        BigDecimal parcelas,

        BigDecimal variavel,

        BigDecimal agendados,

        BigDecimal despesa,

        BigDecimal aportes,

        BigDecimal resultado,

        BigDecimal saldoFinal

) {}