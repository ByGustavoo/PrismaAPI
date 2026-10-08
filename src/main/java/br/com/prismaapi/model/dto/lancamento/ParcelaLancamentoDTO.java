package br.com.prismaapi.model.dto.lancamento;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Identifica a parcela de compra parcelada cujo pagamento gerou o Lançamento.")
public record ParcelaLancamentoDTO(

        UUID idCompra,

        Integer numero,

        Integer total

) {}