package br.com.prismaapi.repository.lancamento;

import br.com.prismaapi.model.dto.lancamento.FiltroLancamentoDTO;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LancamentoSpecification {

    private LancamentoSpecification() {
    }

    public static Specification<Lancamento> filtrar(FiltroLancamentoDTO filtro) {
        return (root, query, builder) -> {
            var predicados = new ArrayList<Predicate>();

            if (filtro.tipo() != null) {
                predicados.add(builder.equal(root.get("tipo"), filtro.tipo()));
            }

            if (filtro.situacao() != null) {
                predicados.add(builder.equal(root.get("situacao"), filtro.situacao()));
            }

            if (filtro.dataInicial() != null) {
                predicados.add(builder.greaterThanOrEqualTo(root.<LocalDate>get("data"), filtro.dataInicial()));
            }

            if (filtro.dataFinal() != null) {
                predicados.add(builder.lessThanOrEqualTo(root.<LocalDate>get("data"), filtro.dataFinal()));
            }

            if (filtro.idCategoria() != null || filtro.idOrigem() != null || temBusca(filtro)) {
                predicados.addAll(filtrarPorRelacionamentos(filtro, root, builder));
            }

            return builder.and(predicados.toArray(Predicate[]::new));
        };
    }

    private static List<Predicate> filtrarPorRelacionamentos(FiltroLancamentoDTO filtro, Root<Lancamento> root, CriteriaBuilder builder) {
        var categoria = root.join("categoria", JoinType.LEFT);
        var conta = root.join("conta", JoinType.LEFT);
        var cartao = root.join("cartao", JoinType.LEFT);
        var contaDestino = root.join("contaDestino", JoinType.LEFT);

        var predicados = new ArrayList<Predicate>();

        if (filtro.idCategoria() != null) {
            predicados.add(builder.equal(categoria.get("id"), filtro.idCategoria()));
        }

        if (filtro.idOrigem() != null) {
            predicados.add(builder.or(
                    builder.equal(conta.get("id"), filtro.idOrigem()),
                    builder.equal(cartao.get("id"), filtro.idOrigem()),
                    builder.equal(contaDestino.get("id"), filtro.idOrigem())));
        }

        if (temBusca(filtro)) {
            var padrao = "%" + normalizar(filtro.busca()) + "%";

            predicados.add(builder.or(
                    contem(builder, root.get("descricao"), padrao),
                    contem(builder, categoria.get("nome"), padrao),
                    contem(builder, conta.get("nome"), padrao),
                    contem(builder, cartao.get("nome"), padrao),
                    contem(builder, contaDestino.get("nome"), padrao)));
        }

        return predicados;
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

    private static boolean temBusca(FiltroLancamentoDTO filtro) {
        return filtro.busca() != null && !filtro.busca().isBlank();
    }
}