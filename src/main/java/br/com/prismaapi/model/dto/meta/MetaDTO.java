package br.com.prismaapi.model.dto.meta;

import br.com.prismaapi.enums.SituacaoMeta;
import br.com.prismaapi.model.dto.metapreco.MetaPrecoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de uma Meta.")
public record MetaDTO(

        UUID id,

        String nome,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String url,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String urlImagem,

        SituacaoMeta situacao,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String observacoes,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataCriacao,

        List<MetaPrecoDTO> historico

) {}