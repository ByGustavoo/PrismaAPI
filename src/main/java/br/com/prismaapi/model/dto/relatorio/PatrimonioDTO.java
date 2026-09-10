package br.com.prismaapi.model.dto.relatorio;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.YearMonth;

@Schema(description = "Representa o patrimônio de um mês separado entre contas e investimentos.")
public record PatrimonioDTO(

        @JsonFormat(pattern = "yyyy-MM")
        YearMonth mes,

        String rotulo,

        BigDecimal contas,

        BigDecimal investimentos,

        BigDecimal total

) {}