package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EstadoAviso {

    NOVO("NOVO"),
    LIDO("LIDO"),
    ADIADO("ADIADO");

    private final String descricao;
}