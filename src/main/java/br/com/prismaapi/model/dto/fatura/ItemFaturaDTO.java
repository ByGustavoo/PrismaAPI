package br.com.prismaapi.model.dto.fatura;

import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Representa um item de fatura: uma despesa lançada no cartão ou a parcela de uma compra parcelada.")
public record ItemFaturaDTO(

        String id,

        String descricao,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate data,

        BigDecimal valor,

        CategoriaDTO categoria,

        ParcelaItemFaturaDTO parcela

) {}