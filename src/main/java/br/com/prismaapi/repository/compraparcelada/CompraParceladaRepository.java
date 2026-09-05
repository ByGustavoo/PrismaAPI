package br.com.prismaapi.repository.compraparcelada;

import br.com.prismaapi.model.entity.compraparcelada.CompraParcelada;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface CompraParceladaRepository extends JpaRepository<CompraParcelada, UUID> {

    boolean existsByCartaoId(UUID cartaoId);

    boolean existsByCategoriaId(UUID categoriaId);

    Page<CompraParcelada> findByCartaoId(UUID cartaoId, Pageable pageable);

    Page<CompraParcelada> findByCartaoIdAndPrimeiroMes(UUID cartaoId, LocalDate primeiroMes, Pageable pageable);

}
