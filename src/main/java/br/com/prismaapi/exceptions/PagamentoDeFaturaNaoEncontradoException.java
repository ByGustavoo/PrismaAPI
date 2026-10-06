package br.com.prismaapi.exceptions;

public class PagamentoDeFaturaNaoEncontradoException extends RuntimeException {

    public PagamentoDeFaturaNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}