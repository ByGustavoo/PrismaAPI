package br.com.prismaapi.exceptions;

public class PagamentoDeParcelaNaoEncontradoException extends RuntimeException {

    public PagamentoDeParcelaNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}