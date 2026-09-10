package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GrupoOrigem {

    CONTA("CONTA"),
    CARTAO("CARTÃO");

    private final String descricao;
}