package br.com.prismaapi.exceptions;

public class ParcelaJaPagaException extends RuntimeException {

    public ParcelaJaPagaException(String mensagem) {
        super(mensagem);
    }
}