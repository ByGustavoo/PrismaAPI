package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SituacaoDespesaRecorrente {

    ATIVO("ATIVO"),
    PAUSADO("PAUSADO");

    private final String descricao;

}