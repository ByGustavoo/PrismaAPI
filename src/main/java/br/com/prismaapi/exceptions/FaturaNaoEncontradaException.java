package br.com.prismaapi.exceptions;

public class FaturaNaoEncontradaException extends RuntimeException {

    public FaturaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}