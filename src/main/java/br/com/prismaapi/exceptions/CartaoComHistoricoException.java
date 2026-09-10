package br.com.prismaapi.exceptions;

public class CartaoComHistoricoException extends RuntimeException {

    public CartaoComHistoricoException(String mensagem) {
        super(mensagem);
    }
}