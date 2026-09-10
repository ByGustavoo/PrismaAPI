package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Situacao {

    ATIVO("ATIVO"),
    INATIVO("INATIVO");

    private final String descricao;

}