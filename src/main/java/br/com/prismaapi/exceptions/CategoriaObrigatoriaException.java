package br.com.prismaapi.exceptions;

public class CategoriaObrigatoriaException extends RuntimeException {

    public CategoriaObrigatoriaException(String mensagem) {
        super(mensagem);
    }
}