package br.com.prismaapi.exceptions;

public class PrecoDuplicadoException extends RuntimeException {

    public PrecoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}