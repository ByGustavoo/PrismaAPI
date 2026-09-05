package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoCategoria {

    RECEITA("Receita"),
    DESPESA("Despesa");

    private final String descricao;

}
