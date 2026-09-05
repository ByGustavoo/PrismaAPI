package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SituacaoDespesaRecorrente {

    ATIVO("Ativo"),
    PAUSADO("Pausado");

    private final String descricao;

}
