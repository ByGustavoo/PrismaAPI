package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SituacaoMeta {

    COMPRADA("COMPRADA"),
    CANCELADA("CANCELADA"),
    ACOMPANHANDO("ACOMPANHANDO");

    private final String descricao;

}