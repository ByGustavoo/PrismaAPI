package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClasseAtivo {

    CDB("CDB"),
    ETF("ETF"),
    ACOES("AÇÕES"),
    CRIPTO("CRIPTO"),
    OUTROS("OUTROS"),
    FUNDOS("FUNDOS"),
    RENDA_FIXA("RENDA FIXA"),
    TESOURO("TESOURO DIRETO");

    private final String descricao;

}