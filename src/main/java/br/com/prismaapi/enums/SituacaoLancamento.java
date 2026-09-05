package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SituacaoLancamento {

    PAGO("Pago"),
    PENDENTE("Pendente"),
    AGENDADO("Agendado");

    private final String descricao;

}
