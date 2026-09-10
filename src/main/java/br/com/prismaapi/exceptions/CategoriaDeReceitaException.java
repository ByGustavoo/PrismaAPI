package br.com.prismaapi.exceptions;

public class CategoriaDeReceitaException extends RuntimeException {

    public CategoriaDeReceitaException(String mensagem) {
        super(mensagem);
    }
}