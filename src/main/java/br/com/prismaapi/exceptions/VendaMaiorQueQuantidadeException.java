package br.com.prismaapi.exceptions;

public class VendaMaiorQueQuantidadeException extends RuntimeException {

    public VendaMaiorQueQuantidadeException(String mensagem) {
        super(mensagem);
    }
}