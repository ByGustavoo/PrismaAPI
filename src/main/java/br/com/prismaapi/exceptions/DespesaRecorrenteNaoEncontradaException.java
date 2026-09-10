package br.com.prismaapi.exceptions;

public class DespesaRecorrenteNaoEncontradaException extends RuntimeException {

    public DespesaRecorrenteNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}