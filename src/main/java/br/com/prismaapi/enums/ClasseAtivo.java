package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClasseAtivo {

    CDB("CDB"),
    RDB("RDB"),
    ETF("ETF"),
    ACOES("AÇÕES"),
    CRIPTO("CRIPTO"),
    OUTROS("OUTROS"),
    FUNDOS("FUNDOS"),
    RENDA_FIXA("RENDA FIXA"),
    TESOURO("TESOURO DIRETO"),
    PREVIDENCIA("PREVIDÊNCIA PRIVADA");

    private final String descricao;

}