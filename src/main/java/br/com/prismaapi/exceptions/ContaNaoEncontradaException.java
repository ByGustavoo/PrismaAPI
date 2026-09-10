package br.com.prismaapi.exceptions;

public class ContaNaoEncontradaException extends RuntimeException {

    public ContaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}