package br.com.prismaapi.model.dto.sistema;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Representa a versão publicada da API.")
public record VersaoSistemaDTO(

        @Schema(description = "Versão declarada no build, sem o prefixo v", example = "1.0.0")
        String versao,

        @Schema(description = "Instante em UTC em que o artefato foi gerado", example = "2026-09-17T14:32:05Z")
        Instant dataLancamento

) {}