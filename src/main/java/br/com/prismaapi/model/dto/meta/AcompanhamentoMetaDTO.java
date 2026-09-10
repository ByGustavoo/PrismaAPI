package br.com.prismaapi.model.dto.meta;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representa uma Meta com a análise do histórico já calculada.")
public record AcompanhamentoMetaDTO(

        MetaDTO meta,

        AnaliseMetaDTO analise

) {}