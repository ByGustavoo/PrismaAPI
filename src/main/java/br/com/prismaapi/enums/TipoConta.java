package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoConta {

    OUTRA("OUTRA"),
    SALARIO("CONTA SALÁRIO"),
    CORRENTE("CONTA CORRENTE");

    private final String descricao;
}