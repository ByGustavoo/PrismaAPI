package br.com.prismaapi.repository.recorrente;

import br.com.prismaapi.enums.SituacaoRecorrente;
import br.com.prismaapi.model.entity.recorrente.Recorrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecorrenteRepository extends JpaRepository<Recorrente, UUID> {

    long countByContaId(UUID contaId);

    long countByCartaoId(UUID cartaoId);

    List<Recorrente> findBySituacao(SituacaoRecorrente situacao);

    @Query("""
            SELECT recorrente
            FROM Recorrente recorrente
            LEFT JOIN FETCH recorrente.categoria
            LEFT JOIN FETCH recorrente.conta
            LEFT JOIN FETCH recorrente.cartao
            """)
    List<Recorrente> buscarComOrigemECategoria();
}