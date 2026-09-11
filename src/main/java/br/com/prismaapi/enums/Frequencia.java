package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

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

    public LocalDate proximaOcorrencia(LocalDate data) {
        return switch (this) {
            case SEMANAL -> data.plusWeeks(1);
            case QUINZENAL -> data.plusWeeks(2);
            case MENSAL -> data.plusMonths(1);
            case BIMESTRAL -> data.plusMonths(2);
            case TRIMESTRAL -> data.plusMonths(3);
            case SEMESTRAL -> data.plusMonths(6);
            case ANUAL -> data.plusYears(1);
        };
    }
}