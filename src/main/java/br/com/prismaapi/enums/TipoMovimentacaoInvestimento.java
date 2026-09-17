package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoMovimentacaoInvestimento {

    APORTE("APORTE"),
    RENDIMENTO("RENDIMENTO");

    private final String descricao;
}