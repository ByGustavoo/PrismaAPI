package br.com.prismaapi.exceptions;

public class OrcamentoDuplicadoException extends RuntimeException {

    public OrcamentoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}