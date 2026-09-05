package br.com.prismaapi.repository.orcamento;

import br.com.prismaapi.model.entity.orcamento.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, UUID> {

    boolean existsByCategoriaId(UUID categoriaId);

    Optional<Orcamento> findByCategoriaId(UUID categoriaId);

}
