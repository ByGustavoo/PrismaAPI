package br.com.prismaapi.exceptions;

public class MetaNaoEncontradaException extends RuntimeException {

    public MetaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}