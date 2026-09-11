package br.com.prismaapi.exceptions;

public class LancamentoFuturoConcluidoException extends RuntimeException {

    public LancamentoFuturoConcluidoException(String mensagem) {
        super(mensagem);
    }
}