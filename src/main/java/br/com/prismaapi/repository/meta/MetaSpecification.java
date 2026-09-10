package br.com.prismaapi.repository.meta;

import br.com.prismaapi.enums.SituacaoMeta;
import br.com.prismaapi.model.entity.meta.Meta;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

public final class MetaSpecification {

    private MetaSpecification() {
    }

    public static Specification<Meta> filtrar(SituacaoMeta situacao, String busca) {
        return (root, query, builder) -> {
            var predicados = new ArrayList<Predicate>();

            if (situacao != null) {
                predicados.add(builder.equal(root.get("situacao"), situacao));
            }

            if (busca != null && !busca.isBlank()) {
                var padrao = "%" + normalizar(busca) + "%";

                predicados.add(builder.or(
                        contem(builder, root.get("nome"), padrao),
                        contem(builder, root.get("observacoes"), padrao)));
            }

            return builder.and(predicados.toArray(Predicate[]::new));
        };
    }

    private static Predicate contem(CriteriaBuilder builder, Expression<String> campo, String padrao) {
        var campoSemAcento = builder.function("unaccent", String.class, builder.lower(campo));
        return builder.like(campoSemAcento, padrao, '\\');
    }

    private static String normalizar(String busca) {
        var semAcento = Normalizer.normalize(busca.strip(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return escaparCuringas(semAcento.toLowerCase(Locale.ROOT));
    }

    private static String escaparCuringas(String termo) {
        return termo.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}