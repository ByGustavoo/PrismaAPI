package br.com.prismaapi.exceptions;

public class CartaoSemContaDePagamentoException extends RuntimeException {

    public CartaoSemContaDePagamentoException(String mensagem) {
        super(mensagem);
    }
}