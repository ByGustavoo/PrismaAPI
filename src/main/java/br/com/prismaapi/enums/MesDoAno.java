package br.com.prismaapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Month;
import java.time.YearMonth;
import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum MesDoAno {

    MAIO(Month.MAY, "Mai"),
    JUNHO(Month.JUNE, "Jun"),
    JULHO(Month.JULY, "Jul"),
    MARCO(Month.MARCH, "Mar"),
    ABRIL(Month.APRIL, "Abr"),
    AGOSTO(Month.AUGUST, "Ago"),
    JANEIRO(Month.JANUARY, "Jan"),
    OUTUBRO(Month.OCTOBER, "Out"),
    NOVEMBRO(Month.NOVEMBER, "Nov"),
    DEZEMBRO(Month.DECEMBER, "Dez"),
    FEVEREIRO(Month.FEBRUARY, "Fev"),
    SETEMBRO(Month.SEPTEMBER, "Set");

    private final Month referencia;
    private final String descricao;

    public static String rotulo(YearMonth mes) {
        return Arrays.stream(values())
                .filter(mesDoAno -> mesDoAno.referencia == mes.getMonth())
                .findFirst()
                .orElseThrow()
                .getDescricao();
    }
}