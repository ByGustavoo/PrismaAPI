package br.com.prismaapi.exceptions;

public class LancamentoNaoEncontradoException extends RuntimeException {

    public LancamentoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}