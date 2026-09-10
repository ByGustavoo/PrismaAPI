package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SituacaoFatura {

    PAGA("PAGA"),
    ABERTA("ABERTA"),
    FUTURA("FUTURA"),
    FECHADA("FECHADA"),
    VENCIDA("VENCIDA");

    private final String descricao;
}