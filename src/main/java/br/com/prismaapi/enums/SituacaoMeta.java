package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SituacaoMeta {

    ACOMPANHANDO("Acompanhando"),
    COMPRADA("Comprada"),
    CANCELADA("Cancelada");

    private final String descricao;

}
