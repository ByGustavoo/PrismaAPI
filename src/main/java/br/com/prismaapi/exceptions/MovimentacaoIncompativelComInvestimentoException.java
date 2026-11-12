package br.com.prismaapi.exceptions;

public class MovimentacaoIncompativelComInvestimentoException extends RuntimeException {

    public MovimentacaoIncompativelComInvestimentoException(String mensagem) {
        super(mensagem);
    }
}