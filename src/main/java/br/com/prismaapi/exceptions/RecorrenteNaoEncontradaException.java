package br.com.prismaapi.exceptions;

public class RecorrenteNaoEncontradaException extends RuntimeException {

    public RecorrenteNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}