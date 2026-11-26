package br.com.prismaapi.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import java.text.Normalizer;
import java.util.Locale;

public final class BuscaSemAcento {

    private BuscaSemAcento() {
    }

    public static boolean informada(String busca) {
        return busca != null && !busca.isBlank();
    }

    public static String padrao(String busca) {
        var semAcento = Normalizer.normalize(busca.strip(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return "%" + escaparCuringas(semAcento.toLowerCase(Locale.ROOT)) + "%";
    }

    public static Predicate contem(CriteriaBuilder builder, Expression<String> campo, String padrao) {
        var campoSemAcento = builder.function("unaccent", String.class, builder.lower(campo));
        return builder.like(campoSemAcento, padrao, '\\');
    }

    private static String escaparCuringas(String termo) {
        return termo.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}