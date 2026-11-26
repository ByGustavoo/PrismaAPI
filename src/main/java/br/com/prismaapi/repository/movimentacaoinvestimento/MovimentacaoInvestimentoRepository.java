package br.com.prismaapi.repository.movimentacaoinvestimento;

import br.com.prismaapi.model.entity.movimentacaoinvestimento.MovimentacaoInvestimento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MovimentacaoInvestimentoRepository extends JpaRepository<MovimentacaoInvestimento, UUID> {

    @EntityGraph(attributePaths = "conta")
    List<MovimentacaoInvestimento> findByInvestimentoId(UUID investimentoId);

    @Query("""
            SELECT movimentacao
            FROM MovimentacaoInvestimento movimentacao
            LEFT JOIN FETCH movimentacao.conta
            WHERE movimentacao.data <= :fim
            """)
    List<MovimentacaoInvestimento> buscarComContaAte(@Param("fim") LocalDate fim);
}