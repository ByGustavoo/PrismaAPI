package br.com.prismaapi.exceptions;

public class DataAnteriorAUltimaAtualizacaoException extends RuntimeException {

    public DataAnteriorAUltimaAtualizacaoException(String mensagem) {
        super(mensagem);
    }
}