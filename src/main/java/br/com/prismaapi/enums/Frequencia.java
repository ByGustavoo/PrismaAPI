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

    public LocalDate ocorrenciaAnterior(LocalDate data) {
        return switch (this) {
            case SEMANAL -> data.minusWeeks(1);
            case QUINZENAL -> data.minusWeeks(2);
            case MENSAL -> data.minusMonths(1);
            case BIMESTRAL -> data.minusMonths(2);
            case TRIMESTRAL -> data.minusMonths(3);
            case SEMESTRAL -> data.minusMonths(6);
            case ANUAL -> data.minusYears(1);
        };
    }
}