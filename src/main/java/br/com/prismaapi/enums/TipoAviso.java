package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoAviso {

    CONTA_VENCENDO("CONTA VENCENDO"),
    LIMITE_CARTAO("LIMITE DO CARTÃO"),
    FATURA_VENCENDO("FATURA VENCENDO"),
    RECEITA_PREVISTA("RECEITA PREVISTA"),
    RECORRENTE_VENCENDO("RECORRENTE VENCENDO"),
    LANCAMENTO_AGENDADO("LANÇAMENTO AGENDADO");

    private final String descricao;
}