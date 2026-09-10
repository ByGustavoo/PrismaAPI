package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SituacaoParcela {

    PAGA("PAGA"),
    ATUAL("ATUAL"),
    FUTURA("FUTURA");

    private final String descricao;
}