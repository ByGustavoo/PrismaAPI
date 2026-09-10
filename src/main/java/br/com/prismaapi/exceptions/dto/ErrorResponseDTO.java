package br.com.prismaapi.exceptions.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Estrutura genérica para respostas de erro da API")
public record ErrorResponseDTO(

        @Schema(description = "Código HTTP")
        int status,

        @Schema(description = "Título resumido")
        String title,

        @Schema(description = "Endpoint da requisição")
        String instance,

        @Schema(description = "URI identificadora do tipo de erro")
        URI type,

        @Schema(description = "Mensagem detalhada")
        String detail,

        @Schema(description = "Erros relacionados à requisição")
        Object errors,

        @Schema(description = "Data e hora do erro")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy - HH:mm:ss")
        LocalDateTime timestamp

) {
    public ErrorResponseDTO(int pStatus, String pTitle, String pInstance, String pType, String pDetail, Object pErrors) {
        this(pStatus, pTitle, pInstance, URI.create(pType), pDetail, pErrors, LocalDateTime.now());
    }

    public ErrorResponseDTO(int pStatus, String pTitle, String pInstance, String pType, String pDetail) {
        this(pStatus, pTitle, pInstance, URI.create(pType), pDetail, null, LocalDateTime.now());
    }
}