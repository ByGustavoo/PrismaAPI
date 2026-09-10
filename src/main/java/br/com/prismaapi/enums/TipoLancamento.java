package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoLancamento {

    DESPESA("DESPESA"),
    RECEITA("RECEITA"),
    TRANSFERENCIA("TRANSFERÊNCIA");

    private final String descricao;

}