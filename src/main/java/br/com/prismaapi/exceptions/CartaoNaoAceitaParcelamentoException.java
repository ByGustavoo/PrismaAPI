package br.com.prismaapi.exceptions;

public class CartaoNaoAceitaParcelamentoException extends RuntimeException {

    public CartaoNaoAceitaParcelamentoException(String mensagem) {
        super(mensagem);
    }
}