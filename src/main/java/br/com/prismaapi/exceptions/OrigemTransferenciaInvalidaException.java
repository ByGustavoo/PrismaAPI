package br.com.prismaapi.exceptions;

public class OrigemTransferenciaInvalidaException extends RuntimeException {

    public OrigemTransferenciaInvalidaException(String mensagem) {
        super(mensagem);
    }
}