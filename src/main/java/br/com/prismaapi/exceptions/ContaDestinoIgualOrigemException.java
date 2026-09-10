package br.com.prismaapi.exceptions;

public class ContaDestinoIgualOrigemException extends RuntimeException {

    public ContaDestinoIgualOrigemException(String mensagem) {
        super(mensagem);
    }
}