package br.com.prismaapi.model.dto.investimento;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Representa um investimento com os números da carteira já calculados.")
public record PosicaoDTO(

        InvestimentoDTO investimento,

        BigDecimal rendimento,

        BigDecimal rentabilidade,

        BigDecimal participacao

) {}