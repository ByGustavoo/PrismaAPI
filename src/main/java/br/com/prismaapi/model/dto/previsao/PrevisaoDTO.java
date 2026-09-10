package br.com.prismaapi.model.dto.previsao;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Representa a previsão financeira dos próximos meses.")
public record PrevisaoDTO(

        BigDecimal saldoInicial,

        List<MesPrevisaoDTO> meses,

        BigDecimal saldoFinal,

        BigDecimal resultadoMedio,

        MenorSaldoDTO menorSaldo

) {}