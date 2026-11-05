package br.com.prismaapi.exceptions;

public class OrigemDeReceitaInvalidaException extends RuntimeException {

    public OrigemDeReceitaInvalidaException(String mensagem) {
        super(mensagem);
    }
}