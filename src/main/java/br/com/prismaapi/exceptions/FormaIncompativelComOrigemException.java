package br.com.prismaapi.exceptions;

public class FormaIncompativelComOrigemException extends RuntimeException {

    public FormaIncompativelComOrigemException(String mensagem) {
        super(mensagem);
    }
}