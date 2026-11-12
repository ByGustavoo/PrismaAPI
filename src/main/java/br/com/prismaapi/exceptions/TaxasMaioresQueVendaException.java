package br.com.prismaapi.exceptions;

public class TaxasMaioresQueVendaException extends RuntimeException {

    public TaxasMaioresQueVendaException(String mensagem) {
        super(mensagem);
    }
}