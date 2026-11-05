package br.com.prismaapi.model.dto.conta;

import br.com.prismaapi.enums.GrupoOrigem;
import br.com.prismaapi.enums.TipoCartao;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Representa uma conta ou um cartão que pode ser escolhido como origem de um Lançamento.")
public record OrigemDTO(

        UUID id,

        String nome,

        GrupoOrigem grupo,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        TipoCartao tipoCartao

) {}