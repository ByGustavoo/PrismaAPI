package br.com.prismaapi.model.dto.relatorio;

import java.time.LocalDate;

public record BaldeRelatorio(

        String rotulo,

        LocalDate inicio,

        LocalDate fim

) {

    public boolean contem(LocalDate data) {
        return !data.isBefore(inicio) && !data.isAfter(fim);
    }
}