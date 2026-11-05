package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoRecorrente {

    RECEITA("RECEITA"),
    DESPESA("DESPESA");

    private final String descricao;

}