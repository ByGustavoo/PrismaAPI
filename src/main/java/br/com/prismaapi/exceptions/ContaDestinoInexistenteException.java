package br.com.prismaapi.exceptions;

public class ContaDestinoInexistenteException extends RuntimeException {

    public ContaDestinoInexistenteException(String mensagem) {
        super(mensagem);
    }
}