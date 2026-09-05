package br.com.prismaapi.repository.despesarecorrente;

import br.com.prismaapi.enums.SituacaoDespesaRecorrente;
import br.com.prismaapi.model.entity.despesarecorrente.DespesaRecorrente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DespesaRecorrenteRepository extends JpaRepository<DespesaRecorrente, UUID> {

    boolean existsByCategoriaId(UUID categoriaId);

    boolean existsByContaId(UUID contaId);

    boolean existsByCartaoId(UUID cartaoId);

    Page<DespesaRecorrente> findBySituacao(SituacaoDespesaRecorrente situacao, Pageable pageable);

    List<DespesaRecorrente> findBySituacaoAndProximoVencimentoLessThanEqual(SituacaoDespesaRecorrente situacao, LocalDate limite);

}
