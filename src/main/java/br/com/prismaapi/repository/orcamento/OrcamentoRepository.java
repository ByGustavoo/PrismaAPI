package br.com.prismaapi.repository.orcamento;

import br.com.prismaapi.model.entity.orcamento.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, UUID> {

    boolean existsByCategoriaId(UUID categoriaId);

    boolean existsByCategoriaIdAndIdNot(UUID categoriaId, UUID id);

    Optional<Orcamento> findByCategoriaId(UUID categoriaId);

    @Query("""
            SELECT orcamento
            FROM Orcamento orcamento
            JOIN FETCH orcamento.categoria
            """)
    List<Orcamento> buscarComCategoria();
}