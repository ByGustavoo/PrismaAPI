package br.com.prismaapi.repository.movimentacaoinvestimento;

import br.com.prismaapi.model.entity.movimentacaoinvestimento.MovimentacaoInvestimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovimentacaoInvestimentoRepository extends JpaRepository<MovimentacaoInvestimento, UUID> {

    List<MovimentacaoInvestimento> findByInvestimentoId(UUID investimentoId);
}