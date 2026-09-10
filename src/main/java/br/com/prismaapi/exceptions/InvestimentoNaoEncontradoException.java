package br.com.prismaapi.exceptions;

public class InvestimentoNaoEncontradoException extends RuntimeException {

    public InvestimentoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}