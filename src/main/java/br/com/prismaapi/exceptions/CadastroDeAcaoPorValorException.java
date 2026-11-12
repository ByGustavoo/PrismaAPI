package br.com.prismaapi.exceptions;

public class CadastroDeAcaoPorValorException extends RuntimeException {

    public CadastroDeAcaoPorValorException(String mensagem) {
        super(mensagem);
    }
}