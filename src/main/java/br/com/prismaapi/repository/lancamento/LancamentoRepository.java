package br.com.prismaapi.repository.lancamento;

import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, UUID> {

    boolean existsByCategoriaId(UUID categoriaId);

    boolean existsByContaIdOrContaDestinoId(UUID contaId, UUID contaDestinoId);

    boolean existsByCartaoId(UUID cartaoId);

    Page<Lancamento> findByDataBetween(LocalDate inicio, LocalDate fim, Pageable pageable);

    Page<Lancamento> findByTipoAndDataBetween(TipoLancamento tipo, LocalDate inicio, LocalDate fim, Pageable pageable);

    Page<Lancamento> findByCategoriaIdAndDataBetween(UUID categoriaId, LocalDate inicio, LocalDate fim, Pageable pageable);

}
