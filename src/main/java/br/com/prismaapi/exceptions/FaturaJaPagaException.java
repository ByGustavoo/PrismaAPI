package br.com.prismaapi.exceptions;

public class FaturaJaPagaException extends RuntimeException {

    public FaturaJaPagaException(String mensagem) {
        super(mensagem);
    }
}