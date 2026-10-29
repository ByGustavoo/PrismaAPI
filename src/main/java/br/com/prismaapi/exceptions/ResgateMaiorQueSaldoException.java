package br.com.prismaapi.exceptions;

public class ResgateMaiorQueSaldoException extends RuntimeException {

    public ResgateMaiorQueSaldoException(String mensagem) {
        super(mensagem);
    }
}