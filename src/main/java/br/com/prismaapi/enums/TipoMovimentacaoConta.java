package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoMovimentacaoConta {

    APORTE("APORTE"),
    RESGATE("RESGATE"),
    RENDIMENTO("RENDIMENTO");

    private final String descricao;
}