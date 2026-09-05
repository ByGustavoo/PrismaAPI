package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClasseAtivo {

    RENDA_FIXA("Renda fixa"),
    CDB("CDB"),
    TESOURO("Tesouro Direto"),
    ACOES("Ações"),
    ETF("ETF"),
    FUNDOS("Fundos"),
    CRIPTO("Cripto"),
    OUTROS("Outros");

    private final String descricao;

}
