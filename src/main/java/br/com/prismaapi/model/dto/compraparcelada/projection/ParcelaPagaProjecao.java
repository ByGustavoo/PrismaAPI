package br.com.prismaapi.model.dto.compraparcelada.projection;

import java.time.LocalDate;
import java.util.UUID;

public record ParcelaPagaProjecao(

        UUID compraParceladaId,

        Short numero,

        LocalDate dataPagamento

) {}