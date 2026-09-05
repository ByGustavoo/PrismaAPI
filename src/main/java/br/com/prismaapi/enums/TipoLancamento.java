package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoLancamento {

    RECEITA("Receita"),
    DESPESA("Despesa"),
    TRANSFERENCIA("Transferência");

    private final String descricao;

}
