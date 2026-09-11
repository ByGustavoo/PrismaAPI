package br.com.prismaapi.model.dto.compraparcelada;

import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de uma Compra Parcelada.")
public record CompraParceladaDTO(

        UUID id,

        String descricao,

        BigDecimal valorTotal,

        Short parcelas,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataCompra,

        @JsonFormat(pattern = "yyyy-MM")
        LocalDate primeiroMes,

        UUID idCartao,

        String nomeCartao,

        CategoriaDTO categoria,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String observacoes

) {}