package br.com.prismaapi.model.dto.meta;

import br.com.prismaapi.enums.SituacaoMeta;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de uma Meta.")
public record MetaDTO(

        UUID id,

        String nome,

        String url,

        String urlImagem,

        SituacaoMeta situacao,

        String observacoes,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataCriacao,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataAtualizacao

) {}
