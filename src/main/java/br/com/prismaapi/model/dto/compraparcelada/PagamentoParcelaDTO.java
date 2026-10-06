package br.com.prismaapi.model.dto.compraparcelada;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Representa o pagamento antecipado de uma parcela de uma Compra Parcelada.")
public record PagamentoParcelaDTO(

        UUID idCompra,

        Integer numero,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataPagamento

) {}