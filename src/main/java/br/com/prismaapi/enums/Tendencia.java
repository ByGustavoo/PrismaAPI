package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Tendencia {

    ALTA("ALTA"),
    BAIXA("BAIXA"),
    ESTAVEL("ESTÁVEL");

    private final String descricao;
}