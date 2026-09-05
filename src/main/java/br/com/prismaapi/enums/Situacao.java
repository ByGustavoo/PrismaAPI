package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Situacao {

    ATIVO("Ativo"),
    INATIVO("Inativo");

    private final String descricao;

}
