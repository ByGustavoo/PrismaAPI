package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TipoConta {

    OUTRA("OUTRA", FinalidadeConta.MOVIMENTACAO),
    POUPANCA("POUPANÇA", FinalidadeConta.RESERVA),
    SALARIO("CONTA SALÁRIO", FinalidadeConta.MOVIMENTACAO),
    PREVIDENCIA("PREVIDÊNCIA", FinalidadeConta.RESERVA),
    CORRENTE("CONTA CORRENTE", FinalidadeConta.MOVIMENTACAO),
    EMERGENCIA("RESERVA DE EMERGÊNCIA", FinalidadeConta.RESERVA);

    private final String descricao;
    private final FinalidadeConta finalidade;
}