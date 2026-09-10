package br.com.prismaapi.exceptions;

public class CategoriaInexistenteException extends RuntimeException {

    public CategoriaInexistenteException(String mensagem) {
        super(mensagem);
    }
}