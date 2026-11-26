package br.com.prismaapi.enums;

import java.time.LocalDate;
import java.time.YearMonth;

public enum Frequencia {

    ANUAL,
    MENSAL,
    SEMANAL,
    QUINZENAL,
    BIMESTRAL,
    SEMESTRAL,
    TRIMESTRAL;

    public LocalDate proximaOcorrencia(LocalDate data, int dia) {
        return deslocar(data, 1, dia);
    }

    public LocalDate ocorrenciaAnterior(LocalDate data, int dia) {
        return deslocar(data, -1, dia);
    }

    public LocalDate ocorrenciaDesde(LocalDate data, LocalDate limite, int dia) {
        var ocorrencia = data;

        while (ocorrencia.isBefore(limite)) {
            ocorrencia = proximaOcorrencia(ocorrencia, dia);
        }

        return ocorrencia;
    }

    private LocalDate deslocar(LocalDate data, int sentido, int dia) {
        return switch (this) {
            case SEMANAL -> data.plusWeeks(sentido);
            case QUINZENAL -> data.plusWeeks(2L * sentido);
            case MENSAL -> noDia(YearMonth.from(data).plusMonths(sentido), dia);
            case BIMESTRAL -> noDia(YearMonth.from(data).plusMonths(2L * sentido), dia);
            case TRIMESTRAL -> noDia(YearMonth.from(data).plusMonths(3L * sentido), dia);
            case SEMESTRAL -> noDia(YearMonth.from(data).plusMonths(6L * sentido), dia);
            case ANUAL -> noDia(YearMonth.from(data).plusYears(sentido), dia);
        };
    }

    private static LocalDate noDia(YearMonth mes, int dia) {
        return mes.atDay(Math.min(dia, mes.lengthOfMonth()));
    }
}