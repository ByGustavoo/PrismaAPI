package br.com.prismaapi.model.dto.investimento;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.YearMonth;

@Schema(description = "Representa o valor aportado e o saldo no fim de um mês, numa série de evolução.")
public record PontoEvolucaoDTO(

        String rotulo,

        @JsonFormat(pattern = "yyyy-MM")
        YearMonth mes,

        BigDecimal aportado,

        BigDecimal valor

) {}