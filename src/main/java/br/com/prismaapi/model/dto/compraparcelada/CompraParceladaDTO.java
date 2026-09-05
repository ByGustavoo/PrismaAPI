package br.com.prismaapi.model.dto.compraparcelada;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de uma Compra Parcelada.")
public record CompraParceladaDTO(

        UUID id,

        String descricao,

        BigDecimal valorTotal,

        Short parcelas,

        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dataCompra,

        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate primeiroMes,

        UUID cartaoId,

        UUID categoriaId,

        String observacoes,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataCriacao,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataAtualizacao

) {}
