package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FinalidadeConta {

    RESERVA("RESERVA"),
    MOVIMENTACAO("MOVIMENTAÇÃO");

    private final String descricao;
}