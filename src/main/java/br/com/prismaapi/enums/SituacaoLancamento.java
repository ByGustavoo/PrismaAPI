package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SituacaoLancamento {

    PAGO("PAGO"),
    PENDENTE("PENDENTE"),
    AGENDADO("AGENDADO");

    private final String descricao;

}