package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoCartao {

    CREDITO("Crédito"),
    DEBITO("Débito"),
    VALE_ALIMENTACAO("Vale alimentação"),
    VALE_REFEICAO("Vale refeição");

    private final String descricao;

}
