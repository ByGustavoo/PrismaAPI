package br.com.prismaapi.model.dto.metapreco;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de um Preço de Meta.")
public record MetaPrecoDTO(

        UUID id,

        UUID metaId,

        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate data,

        BigDecimal preco,

        String observacao,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataCriacao

) {}
