package br.com.prismaapi.model.dto.fatura;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Identifica qual parcela de uma compra parcelada o item da fatura representa.")
public record ParcelaItemFaturaDTO(

        Integer numero,

        Integer total,

        UUID idCompra

) {}