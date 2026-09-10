package br.com.prismaapi.exceptions;

public class ContaComCartaoVinculadoException extends RuntimeException {

    public ContaComCartaoVinculadoException(String mensagem) {
        super(mensagem);
    }
}