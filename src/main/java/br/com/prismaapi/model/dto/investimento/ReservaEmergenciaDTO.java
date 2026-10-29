package br.com.prismaapi.model.dto.investimento;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Representa a soma dos investimentos marcados como reserva de emergência.")
public record ReservaEmergenciaDTO(

        BigDecimal aportado,

        BigDecimal valorAtual,

        BigDecimal rendimento,

        Integer quantidade

) {}