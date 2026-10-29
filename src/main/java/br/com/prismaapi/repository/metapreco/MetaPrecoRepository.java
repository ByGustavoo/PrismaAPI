package br.com.prismaapi.repository.metapreco;

import br.com.prismaapi.model.dto.aviso.projection.UltimoPrecoMetaProjecao;
import br.com.prismaapi.model.entity.metapreco.MetaPreco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
            SELECT new br.com.prismaapi.model.dto.aviso.projection.UltimoPrecoMetaProjecao(
                       meta.id,
                       meta.nome,
                       MAX(preco.data))
            FROM MetaPreco preco
            JOIN preco.meta meta
            WHERE meta.situacao = br.com.prismaapi.enums.SituacaoMeta.ACOMPANHANDO
            GROUP BY meta.id, meta.nome
            HAVING MAX(preco.data) <= :limite
            """)
    List<UltimoPrecoMetaProjecao> buscarAcompanhadasSemPrecoDesde(@Param("limite") LocalDate limite);
}