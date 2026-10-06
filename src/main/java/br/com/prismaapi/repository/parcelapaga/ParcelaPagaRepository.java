package br.com.prismaapi.repository.parcelapaga;

import br.com.prismaapi.model.dto.compraparcelada.projection.ParcelaPagaProjecao;
import br.com.prismaapi.model.entity.parcelapaga.ParcelaPaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParcelaPagaRepository extends JpaRepository<ParcelaPaga, UUID> {

    Optional<ParcelaPaga> findByCompraParceladaIdAndNumero(UUID compraParceladaId, Short numero);

    @Query("""
            SELECT new br.com.prismaapi.model.dto.compraparcelada.projection.ParcelaPagaProjecao(
                       pagamento.compraParcelada.id,
                       pagamento.numero,
                       pagamento.dataPagamento)
            FROM ParcelaPaga pagamento
            """)
    List<ParcelaPagaProjecao> buscarPagamentos();

    @Modifying
    @Query("""
            DELETE FROM ParcelaPaga pagamento
            WHERE pagamento.compraParcelada.id = :compraParceladaId
              AND pagamento.numero > :parcelas
            """)
    void deletarAcimaDe(@Param("compraParceladaId") UUID compraParceladaId,
                        @Param("parcelas") Short parcelas);
}