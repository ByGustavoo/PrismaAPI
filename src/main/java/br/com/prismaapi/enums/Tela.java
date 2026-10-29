package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Tela {

    LANCAMENTOS("LANCAMENTOS"),
    RECEITAS("RECEITAS"),
    DESPESAS("DESPESAS"),
    TRANSFERENCIAS("TRANSFERENCIAS"),
    CONTAS("CONTAS"),
    CARTOES("CARTOES"),
    FATURAS("FATURAS"),
    PARCELAMENTOS("PARCELAMENTOS"),
    METAS("METAS"),
    ORCAMENTO("ORCAMENTO"),
    PREVISAO("PREVISAO"),
    RECORRENTES("RECORRENTES"),
    INVESTIMENTOS("INVESTIMENTOS"),
    RELATORIOS("RELATORIOS");

    private final String descricao;
}