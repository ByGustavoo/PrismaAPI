package br.com.prismaapi.exceptions;

public class CartaoNaoEncontradoException extends RuntimeException {

    public CartaoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}