package br.com.prismaapi.exceptions;

public class QuantidadeDeAcoesObrigatoriaException extends RuntimeException {

    public QuantidadeDeAcoesObrigatoriaException(String mensagem) {
        super(mensagem);
    }
}