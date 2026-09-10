package br.com.prismaapi.exceptions;

public class OrigemInexistenteException extends RuntimeException {

    public OrigemInexistenteException(String mensagem) {
        super(mensagem);
    }
}