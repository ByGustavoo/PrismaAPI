package br.com.prismaapi.model.dto.dashboard;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public record PeriodoDashboard(

        YearMonth de,

        YearMonth ate,

        YearMonth inicioDaJanela,

        LocalDate hoje

) {

    private static final int MESES_DA_JANELA_MINIMA = 6;

    public static PeriodoDashboard resolver(YearMonth de, YearMonth ate, LocalDate hoje) {
        var mesAtual = YearMonth.from(hoje);
        var inicio = de != null ? de : mesAtual;
        var fim = ate != null ? ate : mesAtual;

        var inicioDaJanela = inicio.equals(fim)
                ? inicio.minusMonths(MESES_DA_JANELA_MINIMA - 1L)
                : inicio;

        return new PeriodoDashboard(inicio, fim, inicioDaJanela, hoje);
    }

    public PeriodoDashboard anterior() {
        var meses = quantidadeDeMeses();
        return resolver(de.minusMonths(meses), ate.minusMonths(meses), hoje);
    }

    public int quantidadeDeMeses() {
        return (int) ChronoUnit.MONTHS.between(de, ate) + 1;
    }

    public LocalDate primeiroDia() {
        return de.atDay(1);
    }

    public LocalDate ultimoDia() {
        return ate.atEndOfMonth();
    }

    public LocalDate primeiroDiaDaJanela() {
        return inicioDaJanela.atDay(1);
    }

    public LocalDate ultimoDiaDaJanela() {
        return ate.atEndOfMonth();
    }

    public List<YearMonth> mesesDaJanela() {
        var meses = new ArrayList<YearMonth>();
        for (var mes = inicioDaJanela; !mes.isAfter(ate); mes = mes.plusMonths(1)) {
            meses.add(mes);
        }
        return meses;
    }

    public LocalDate dataDeCorte() {
        return dataDeCorte(ate);
    }

    public LocalDate dataDeCorte(YearMonth mes) {
        return mes.equals(YearMonth.from(hoje)) ? hoje : mes.atEndOfMonth();
    }
}