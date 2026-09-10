package br.com.prismaapi.exceptions;

public class CompraParceladaNaoEncontradaException extends RuntimeException {

    public CompraParceladaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}