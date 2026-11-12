package br.com.prismaapi.exceptions;

public class TrocaDeClasseDeAcaoException extends RuntimeException {

    public TrocaDeClasseDeAcaoException(String mensagem) {
        super(mensagem);
    }
}