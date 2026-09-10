package br.com.prismaapi.model.dto.dashboard;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public record PeriodoDashboard(

        YearMonth dataInicial,

        YearMonth dataFinal,

        YearMonth inicioDaJanela,

        LocalDate hoje

) {

    public static PeriodoDashboard resolver(YearMonth dataInicial, YearMonth dataFinal, LocalDate hoje) {
        var mesAtual = YearMonth.from(hoje);
        var inicio = dataInicial != null ? dataInicial : mesAtual;
        var fim = dataFinal != null ? dataFinal : mesAtual;

        var inicioDaJanela = inicio.equals(fim)
                ? inicio.minusMonths(5)
                : inicio;

        return new PeriodoDashboard(inicio, fim, inicioDaJanela, hoje);
    }

    public PeriodoDashboard anterior() {
        var meses = quantidadeDeMeses();
        return resolver(dataInicial.minusMonths(meses), dataFinal.minusMonths(meses), hoje);
    }

    public int quantidadeDeMeses() {
        return (int) ChronoUnit.MONTHS.between(dataInicial, dataFinal) + 1;
    }

    public LocalDate primeiroDia() {
        return dataInicial.atDay(1);
    }

    public LocalDate ultimoDia() {
        return dataFinal.atEndOfMonth();
    }

    public LocalDate primeiroDiaDaJanela() {
        return inicioDaJanela.atDay(1);
    }

    public LocalDate ultimoDiaDaJanela() {
        return dataFinal.atEndOfMonth();
    }

    public List<YearMonth> mesesDaJanela() {
        var meses = new ArrayList<YearMonth>();
        for (var mes = inicioDaJanela; !mes.isAfter(dataFinal); mes = mes.plusMonths(1)) {
            meses.add(mes);
        }
        return meses;
    }

    public LocalDate dataDeCorte() {
        return dataDeCorte(dataFinal);
    }

    public LocalDate dataDeCorte(YearMonth mes) {
        return mes.equals(YearMonth.from(hoje)) ? hoje : mes.atEndOfMonth();
    }
}