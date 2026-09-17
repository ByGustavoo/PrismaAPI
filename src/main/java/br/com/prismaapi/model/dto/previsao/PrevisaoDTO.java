package br.com.prismaapi.model.dto.previsao;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Representa a previsão financeira: o resto do mês corrente e os próximos meses.")
public record PrevisaoDTO(

        BigDecimal saldoAtual,

        MesPrevisaoDTO restanteMesAtual,

        BigDecimal saldoInicial,

        List<MesPrevisaoDTO> meses,

        BigDecimal saldoFinal,

        BigDecimal resultadoMedio,

        MenorSaldoDTO menorSaldo,

        BaseCalculoPrevisaoDTO base

) {}