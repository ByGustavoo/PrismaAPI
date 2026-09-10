package br.com.prismaapi.repository.lancamento;

import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.dto.dashboard.projection.GastoCategoriaProjecao;
import br.com.prismaapi.model.dto.dashboard.projection.MovimentoDiarioProjecao;
import br.com.prismaapi.model.dto.dashboard.projection.TotalMensalProjecao;
import br.com.prismaapi.model.dto.dashboard.projection.ValorPorDataProjecao;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, UUID> {

    boolean existsByCategoriaId(UUID categoriaId);

    boolean existsByContaIdOrContaDestinoId(UUID contaId, UUID contaDestinoId);

    boolean existsByCartaoId(UUID cartaoId);

    Page<Lancamento> findByDataBetween(LocalDate inicio, LocalDate fim, Pageable pageable);

    Page<Lancamento> findByTipoAndDataBetween(TipoLancamento tipo, LocalDate inicio, LocalDate fim, Pageable pageable);

    Page<Lancamento> findByCategoriaIdAndDataBetween(UUID categoriaId, LocalDate inicio, LocalDate fim, Pageable pageable);

    @Query("""
            SELECT SUM(lancamento.valor)
            FROM Lancamento lancamento
            WHERE lancamento.tipo = :tipo
              AND lancamento.data BETWEEN :inicio AND :fim
            """)
    BigDecimal somarPorTipo(@Param("tipo") TipoLancamento tipo,
                            @Param("inicio") LocalDate inicio,
                            @Param("fim") LocalDate fim);

    @Query("""
            SELECT new br.com.prismaapi.model.dto.dashboard.projection.TotalMensalProjecao(
                       YEAR(lancamento.data),
                       MONTH(lancamento.data),
                       lancamento.tipo,
                       SUM(lancamento.valor))
            FROM Lancamento lancamento
            WHERE lancamento.tipo <> br.com.prismaapi.enums.TipoLancamento.TRANSFERENCIA
              AND lancamento.data BETWEEN :inicio AND :fim
            GROUP BY YEAR(lancamento.data), MONTH(lancamento.data), lancamento.tipo
            """)
    List<TotalMensalProjecao> agruparTotaisPorMes(@Param("inicio") LocalDate inicio,
                                                  @Param("fim") LocalDate fim);

    @Query("""
            SELECT new br.com.prismaapi.model.dto.dashboard.projection.ValorPorDataProjecao(
                       lancamento.data,
                       SUM(lancamento.valor))
            FROM Lancamento lancamento
            WHERE lancamento.tipo = br.com.prismaapi.enums.TipoLancamento.DESPESA
              AND lancamento.data BETWEEN :inicio AND :fim
            GROUP BY lancamento.data
            """)
    List<ValorPorDataProjecao> agruparDespesasPorDia(@Param("inicio") LocalDate inicio,
                                                     @Param("fim") LocalDate fim);

    @Query("""
            SELECT new br.com.prismaapi.model.dto.dashboard.projection.GastoCategoriaProjecao(
                       lancamento.categoria.id,
                       SUM(lancamento.valor))
            FROM Lancamento lancamento
            WHERE lancamento.tipo = br.com.prismaapi.enums.TipoLancamento.DESPESA
              AND lancamento.categoria IS NOT NULL
              AND lancamento.data BETWEEN :inicio AND :fim
            GROUP BY lancamento.categoria.id
            ORDER BY SUM(lancamento.valor) DESC
            """)
    List<GastoCategoriaProjecao> agruparDespesasPorCategoria(@Param("inicio") LocalDate inicio,
                                                             @Param("fim") LocalDate fim);

    @Query("""
            SELECT new br.com.prismaapi.model.dto.dashboard.projection.MovimentoDiarioProjecao(
                       lancamento.data,
                       lancamento.tipo,
                       SUM(lancamento.valor))
            FROM Lancamento lancamento
            JOIN lancamento.conta conta
            WHERE lancamento.tipo <> br.com.prismaapi.enums.TipoLancamento.TRANSFERENCIA
              AND conta.situacao = br.com.prismaapi.enums.Situacao.ATIVO
              AND conta.incluirNoTotal = TRUE
              AND lancamento.data > :inicio
              AND lancamento.data <= :fim
            GROUP BY lancamento.data, lancamento.tipo
            """)
    List<MovimentoDiarioProjecao> agruparMovimentoDoTotalPorDia(@Param("inicio") LocalDate inicio,
                                                                @Param("fim") LocalDate fim);

    @Query("""
            SELECT new br.com.prismaapi.model.dto.dashboard.projection.ValorPorDataProjecao(
                       lancamento.data,
                       SUM(lancamento.valor))
            FROM Lancamento lancamento
            JOIN lancamento.conta origem
            JOIN lancamento.contaDestino destino
            WHERE lancamento.tipo = br.com.prismaapi.enums.TipoLancamento.TRANSFERENCIA
              AND origem.situacao = br.com.prismaapi.enums.Situacao.ATIVO
              AND origem.incluirNoTotal = TRUE
              AND (destino.situacao <> br.com.prismaapi.enums.Situacao.ATIVO OR destino.incluirNoTotal = FALSE)
              AND lancamento.data > :inicio
              AND lancamento.data <= :fim
            GROUP BY lancamento.data
            """)
    List<ValorPorDataProjecao> agruparTransferenciasQueSaemDoTotal(@Param("inicio") LocalDate inicio,
                                                                   @Param("fim") LocalDate fim);

    @Query("""
            SELECT new br.com.prismaapi.model.dto.dashboard.projection.ValorPorDataProjecao(
                       lancamento.data,
                       SUM(lancamento.valor))
            FROM Lancamento lancamento
            JOIN lancamento.conta origem
            JOIN lancamento.contaDestino destino
            WHERE lancamento.tipo = br.com.prismaapi.enums.TipoLancamento.TRANSFERENCIA
              AND destino.situacao = br.com.prismaapi.enums.Situacao.ATIVO
              AND destino.incluirNoTotal = TRUE
              AND (origem.situacao <> br.com.prismaapi.enums.Situacao.ATIVO OR origem.incluirNoTotal = FALSE)
              AND lancamento.data > :inicio
              AND lancamento.data <= :fim
            GROUP BY lancamento.data
            """)
    List<ValorPorDataProjecao> agruparTransferenciasQueEntramNoTotal(@Param("inicio") LocalDate inicio,
                                                                     @Param("fim") LocalDate fim);

    @Query("""
            SELECT SUM(lancamento.valor)
            FROM Lancamento lancamento
            WHERE lancamento.tipo = br.com.prismaapi.enums.TipoLancamento.DESPESA
              AND lancamento.cartao.id = :cartaoId
              AND lancamento.data BETWEEN :inicio AND :fim
            """)
    BigDecimal somarDespesasDoCartao(@Param("cartaoId") UUID cartaoId,
                                     @Param("inicio") LocalDate inicio,
                                     @Param("fim") LocalDate fim);

    @Query("""
            SELECT lancamento
            FROM Lancamento lancamento
            LEFT JOIN FETCH lancamento.categoria
            LEFT JOIN FETCH lancamento.conta
            LEFT JOIN FETCH lancamento.cartao
            LEFT JOIN FETCH lancamento.contaDestino
            WHERE lancamento.data BETWEEN :inicio AND :fim
            ORDER BY lancamento.data DESC, lancamento.descricao ASC
            """)
    List<Lancamento> buscarRecentes(@Param("inicio") LocalDate inicio,
                                    @Param("fim") LocalDate fim,
                                    Pageable pageable);
}