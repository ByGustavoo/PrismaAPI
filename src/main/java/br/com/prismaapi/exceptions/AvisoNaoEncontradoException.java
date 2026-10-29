package br.com.prismaapi.exceptions;

public class AvisoNaoEncontradoException extends RuntimeException {

    public AvisoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}