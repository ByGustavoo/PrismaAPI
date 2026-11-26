package br.com.prismaapi.repository.meta;

import br.com.prismaapi.enums.SituacaoMeta;
import br.com.prismaapi.model.entity.meta.Meta;
import br.com.prismaapi.repository.BuscaSemAcento;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

public final class MetaSpecification {

    private MetaSpecification() {
    }

    public static Specification<Meta> filtrar(SituacaoMeta situacao, String busca) {
        return (root, query, builder) -> {
            var predicados = new ArrayList<Predicate>();

            if (situacao != null) {
                predicados.add(builder.equal(root.get("situacao"), situacao));
            }

            if (BuscaSemAcento.informada(busca)) {
                var padrao = BuscaSemAcento.padrao(busca);

                predicados.add(builder.or(
                        BuscaSemAcento.contem(builder, root.get("nome"), padrao),
                        BuscaSemAcento.contem(builder, root.get("observacoes"), padrao)));
            }

            return builder.and(predicados.toArray(Predicate[]::new));
        };
    }
}