package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoCartao {

    CREDITO("CRÉDITO"),
    DEBITO("DÉBITO"),
    VALE_REFEICAO("VALE REFEIÇÃO"),
    VALE_ALIMENTACAO("VALE ALIMENTAÇÃO");

    private final String descricao;

}