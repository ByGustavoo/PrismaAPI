package br.com.prismaapi.exceptions;

public class OrcamentoNaoEncontradoException extends RuntimeException {

    public OrcamentoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}