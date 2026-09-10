package br.com.prismaapi.model.dto.meta;

import br.com.prismaapi.enums.SituacaoMeta;
import br.com.prismaapi.model.dto.metapreco.MetaPrecoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de uma Meta.")
public record MetaDTO(

        UUID id,

        String nome,

        String url,

        String urlImagem,

        SituacaoMeta situacao,

        String observacoes,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataCriacao,

        List<MetaPrecoDTO> historico

) {}