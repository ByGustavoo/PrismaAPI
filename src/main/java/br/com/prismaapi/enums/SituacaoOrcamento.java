package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SituacaoOrcamento {

    ALERTA("ALERTA"),
    SEGURO("SEGURO"),
    ESTOURADO("ESTOURADO");

    private final String descricao;
}