package br.com.prismaapi.exceptions;

public class OrigemCartaoDeDebitoException extends RuntimeException {

    public OrigemCartaoDeDebitoException(String mensagem) {
        super(mensagem);
    }
}