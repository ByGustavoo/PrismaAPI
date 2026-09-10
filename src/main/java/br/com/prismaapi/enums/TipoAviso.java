package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoAviso {

    CONTA_VENCENDO("CONTA VENCENDO"),
    LIMITE_CARTAO("LIMITE DO CARTÃO"),
    FATURA_VENCENDO("FATURA VENCENDO"),
    LANCAMENTO_AGENDADO("LANÇAMENTO AGENDADO");

    private final String descricao;
}