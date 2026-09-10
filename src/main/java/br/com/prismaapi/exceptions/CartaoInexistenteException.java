package br.com.prismaapi.exceptions;

public class CartaoInexistenteException extends RuntimeException {

    public CartaoInexistenteException(String mensagem) {
        super(mensagem);
    }
}