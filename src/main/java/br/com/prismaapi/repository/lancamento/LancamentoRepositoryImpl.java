package br.com.prismaapi.repository.lancamento;

import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class LancamentoRepositoryImpl implements LancamentoRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public BigDecimal somarResultado(Specification<Lancamento> especificacao) {
        var builder = entityManager.getCriteriaBuilder();
        var consulta = builder.createQuery(BigDecimal.class);
        var lancamento = consulta.from(Lancamento.class);

        var valor = lancamento.<BigDecimal>get("valor");
        var tipo = lancamento.<TipoLancamento>get("tipo");
        var valorComSinal = builder.<BigDecimal>selectCase()
                .when(builder.equal(tipo, TipoLancamento.RECEITA), valor)
                .when(builder.equal(tipo, TipoLancamento.DESPESA), builder.neg(valor))
                .otherwise(BigDecimal.ZERO);

        consulta.select(builder.coalesce(builder.sum(valorComSinal), BigDecimal.ZERO))
                .where(especificacao.toPredicate(lancamento, consulta, builder));

        return entityManager.createQuery(consulta).getSingleResult();
    }
}