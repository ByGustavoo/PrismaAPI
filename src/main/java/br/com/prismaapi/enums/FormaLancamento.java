package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FormaLancamento {

    CONTA("Conta"),
    CARTAO_CREDITO("Cartão de crédito"),
    PIX("Pix"),
    DINHEIRO("Dinheiro");

    private final String descricao;

}
