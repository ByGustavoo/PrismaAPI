package br.com.prismaapi.exceptions;

public class ParcelaNaoEncontradaException extends RuntimeException {

    public ParcelaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}