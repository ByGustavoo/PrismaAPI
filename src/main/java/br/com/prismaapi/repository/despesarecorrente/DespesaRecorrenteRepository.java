package br.com.prismaapi.repository.despesarecorrente;

import br.com.prismaapi.enums.SituacaoDespesaRecorrente;
import br.com.prismaapi.model.entity.despesarecorrente.DespesaRecorrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DespesaRecorrenteRepository extends JpaRepository<DespesaRecorrente, UUID> {

    long countByContaId(UUID contaId);

    long countByCartaoId(UUID cartaoId);

    List<DespesaRecorrente> findBySituacao(SituacaoDespesaRecorrente situacao);

    @Query("""
            SELECT despesa
            FROM DespesaRecorrente despesa
            LEFT JOIN FETCH despesa.categoria
            LEFT JOIN FETCH despesa.conta
            LEFT JOIN FETCH despesa.cartao
            """)
    List<DespesaRecorrente> buscarComOrigemECategoria();
}