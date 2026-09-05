package br.com.prismaapi.repository.metapreco;

import br.com.prismaapi.model.entity.metapreco.MetaPreco;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MetaPrecoRepository extends JpaRepository<MetaPreco, UUID> {

    boolean existsByMetaIdAndDataAndPreco(UUID metaId, LocalDate data, BigDecimal preco);

    Page<MetaPreco> findByMetaId(UUID metaId, Pageable pageable);

    Optional<MetaPreco> findFirstByMetaIdOrderByDataDesc(UUID metaId);

}
