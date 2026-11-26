package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SeveridadeAviso {

    CRITICO(1),
    ATENCAO(2),
    INFO(3);

    private final int prioridade;
}