package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Frequencia {

    SEMANAL("Semanal"),
    QUINZENAL("Quinzenal"),
    MENSAL("Mensal"),
    BIMESTRAL("Bimestral"),
    TRIMESTRAL("Trimestral"),
    SEMESTRAL("Semestral"),
    ANUAL("Anual");

    private final String descricao;

}
