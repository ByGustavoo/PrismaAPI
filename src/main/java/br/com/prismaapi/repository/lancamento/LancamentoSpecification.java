package br.com.prismaapi.repository.lancamento;

import br.com.prismaapi.model.dto.lancamento.FiltroLancamentoDTO;
import br.com.prismaapi.model.entity.cartao.Cartao;
import br.com.prismaapi.model.entity.categoria.Categoria;
import br.com.prismaapi.model.entity.conta.Conta;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.repository.BuscaSemAcento;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

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

            if (filtro.idCategoria() != null) {
                predicados.add(builder.equal(root.get("categoria").get("id"), filtro.idCategoria()));
            }

            if (filtro.idOrigem() != null) {
                predicados.add(builder.or(
                        builder.equal(root.get("conta").get("id"), filtro.idOrigem()),
                        builder.equal(root.get("cartao").get("id"), filtro.idOrigem()),
                        builder.equal(root.get("contaDestino").get("id"), filtro.idOrigem())));
            }

            if (BuscaSemAcento.informada(filtro.busca())) {
                predicados.add(buscar(root, query, builder, BuscaSemAcento.padrao(filtro.busca())));
            }

            return builder.and(predicados.toArray(Predicate[]::new));
        };
    }

    private static Predicate buscar(Root<Lancamento> root, CriteriaQuery<?> query, CriteriaBuilder builder, String padrao) {
        return builder.or(
                BuscaSemAcento.contem(builder, root.get("descricao"), padrao),
                BuscaSemAcento.contem(builder, root.get("observacoes"), padrao),
                root.get("categoria").get("id").in(idsComNome(query, builder, Categoria.class, padrao)),
                root.get("conta").get("id").in(idsComNome(query, builder, Conta.class, padrao)),
                root.get("cartao").get("id").in(idsComNome(query, builder, Cartao.class, padrao)),
                root.get("contaDestino").get("id").in(idsComNome(query, builder, Conta.class, padrao)));
    }

    private static Subquery<UUID> idsComNome(CriteriaQuery<?> query, CriteriaBuilder builder, Class<?> entidade, String padrao) {
        var subconsulta = query.subquery(UUID.class);
        var registro = subconsulta.from(entidade);

        return subconsulta
                .select(registro.get("id"))
                .where(BuscaSemAcento.contem(builder, registro.get("nome"), padrao));
    }
}