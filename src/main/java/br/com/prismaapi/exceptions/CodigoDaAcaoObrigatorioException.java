package br.com.prismaapi.exceptions;

public class CodigoDaAcaoObrigatorioException extends RuntimeException {

    public CodigoDaAcaoObrigatorioException(String mensagem) {
        super(mensagem);
    }
}