package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FormaLancamento {

    PIX("PIX"),
    CONTA("CONTA"),
    DINHEIRO("DINHEIRO"),
    CARTAO_CREDITO("CARTÃO DE CRÉDITO");

    private final String descricao;

}