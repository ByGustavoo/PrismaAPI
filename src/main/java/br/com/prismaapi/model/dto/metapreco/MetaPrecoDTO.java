package br.com.prismaapi.model.dto.metapreco;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de um Preço de Meta.")
public record MetaPrecoDTO(

        UUID id,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate data,

        BigDecimal preco,

        String observacao

) {}