package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrdenacaoLancamento {

    DATA("DATA"),
    DESCRICAO("DESCRIÇÃO"),
    VALOR("VALOR");

    private final String descricao;
}