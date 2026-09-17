package br.com.prismaapi.repository.compraparcelada;

import br.com.prismaapi.model.dto.dashboard.projection.ParcelaProjecao;
import br.com.prismaapi.model.entity.compraparcelada.CompraParcelada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CompraParceladaRepository extends JpaRepository<CompraParcelada, UUID> {

    long countByCartaoId(UUID cartaoId);

    @Query("""
            SELECT new br.com.prismaapi.model.dto.dashboard.projection.ParcelaProjecao(
                       compra.cartao.id,
                       compra.valorTotal,
                       compra.parcelas,
                       compra.primeiroMes)
            FROM CompraParcelada compra
            WHERE compra.primeiroMes <= :mes
            """)
    List<ParcelaProjecao> buscarParcelasAte(@Param("mes") LocalDate mes);

    @Query("""
            SELECT new br.com.prismaapi.model.dto.dashboard.projection.ParcelaProjecao(
                       compra.cartao.id,
                       compra.valorTotal,
                       compra.parcelas,
                       compra.primeiroMes)
            FROM CompraParcelada compra
            WHERE compra.cartao.id IN :idsCartoes
            """)
    List<ParcelaProjecao> buscarParcelasDosCartoes(@Param("idsCartoes") List<UUID> idsCartoes);

    @Query("""
            SELECT compra
            FROM CompraParcelada compra
            LEFT JOIN FETCH compra.categoria
            WHERE compra.cartao.id = :cartaoId
              AND compra.primeiroMes <= :mes
            """)
    List<CompraParcelada> buscarDoCartaoAte(@Param("cartaoId") UUID cartaoId,
                                            @Param("mes") LocalDate mes);

    @Query("""
            SELECT compra
            FROM CompraParcelada compra
            JOIN FETCH compra.cartao
            LEFT JOIN FETCH compra.categoria
            """)
    List<CompraParcelada> buscarComCartaoECategoria();

    @Query("""
            SELECT compra
            FROM CompraParcelada compra
            JOIN FETCH compra.cartao
            """)
    List<CompraParcelada> buscarComCartao();
}