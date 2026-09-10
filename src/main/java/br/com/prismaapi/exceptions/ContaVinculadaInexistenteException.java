package br.com.prismaapi.exceptions;

public class ContaVinculadaInexistenteException extends RuntimeException {

    public ContaVinculadaInexistenteException(String mensagem) {
        super(mensagem);
    }
}