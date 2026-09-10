package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SeveridadeAviso {

    CRITICO(1, "CRÍTICO"),
    ATENCAO(2, "ATENÇÃO"),
    INFO(3, "INFORMATIVO");

    private final int prioridade;
    private final String descricao;
}