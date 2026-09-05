package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoConta {

    CORRENTE("Conta corrente"),
    SALARIO("Conta salário"),
    EMERGENCIA("Reserva de emergência"),
    OUTRA("Outra");

    private final String descricao;

}
