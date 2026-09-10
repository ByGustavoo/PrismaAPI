package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeituraMeta {

    MAIOR("MAIOR"),
    MENOR("MENOR"),
    ESTAVEL("ESTÁVEL"),
    PRIMEIRO("PRIMEIRO"),
    ACIMA_DA_MEDIA("ACIMA DA MÉDIA"),
    ABAIXO_DA_MEDIA("ABAIXO DA MÉDIA");

    private final String descricao;
}