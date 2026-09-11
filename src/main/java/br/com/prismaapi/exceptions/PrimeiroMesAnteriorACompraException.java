package br.com.prismaapi.exceptions;

public class PrimeiroMesAnteriorACompraException extends RuntimeException {

    public PrimeiroMesAnteriorACompraException(String mensagem) {
        super(mensagem);
    }
}