package br.com.prismaapi.exceptions;

public class DataAnteriorAoPrimeiroPrecoException extends RuntimeException {

    public DataAnteriorAoPrimeiroPrecoException(String mensagem) {
        super(mensagem);
    }
}