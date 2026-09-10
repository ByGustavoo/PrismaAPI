package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Frequencia {

    ANUAL("ANUAL"),
    MENSAL("MENSAL"),
    SEMANAL("SEMANAL"),
    QUINZENAL("QUINZENAL"),
    BIMESTRAL("BIMESTRAL"),
    SEMESTRAL("SEMESTRAL"),
    TRIMESTRAL("TRIMESTRAL");

    private final String descricao;

}