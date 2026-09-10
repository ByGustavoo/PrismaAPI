package br.com.prismaapi.exceptions;

public class ContaDuplicadaException extends RuntimeException {

    public ContaDuplicadaException(String mensagem) {
        super(mensagem);
    }
}