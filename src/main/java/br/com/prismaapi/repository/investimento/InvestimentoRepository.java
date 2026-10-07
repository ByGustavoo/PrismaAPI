package br.com.prismaapi.repository.investimento;

import br.com.prismaapi.model.dto.dashboard.projection.CarteiraProjecao;
import br.com.prismaapi.model.entity.investimento.Investimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface InvestimentoRepository extends JpaRepository<Investimento, UUID> {

    List<Investimento> findByDataUltimaMovimentacaoLessThanEqual(LocalDate data);

    @Query("""
            SELECT new br.com.prismaapi.model.dto.dashboard.projection.CarteiraProjecao(
                       SUM(investimento.valorAtual),
                       SUM(investimento.aportado))
            FROM Investimento investimento
            """)
    CarteiraProjecao resumirCarteira();
}