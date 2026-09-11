package br.com.prismaapi.repository.metapreco;

import br.com.prismaapi.model.entity.metapreco.MetaPreco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface MetaPrecoRepository extends JpaRepository<MetaPreco, UUID> {

    boolean existsByMetaIdAndDataAndPreco(UUID metaId, LocalDate data, BigDecimal preco);

    List<MetaPreco> findByMetaIdOrderByDataAscDataCriacaoAsc(UUID metaId);

    List<MetaPreco> findByMetaIdInOrderByDataAscDataCriacaoAsc(Collection<UUID> idsMetas);
}