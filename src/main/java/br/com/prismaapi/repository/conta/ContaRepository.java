package br.com.prismaapi.repository.conta;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.model.entity.conta.Conta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContaRepository extends JpaRepository<Conta, UUID> {

    boolean existsByNomeIgnoreCaseAndInstituicaoIgnoreCase(String nome, String instituicao);

    boolean existsByNomeIgnoreCaseAndInstituicaoIgnoreCaseAndIdNot(String nome, String instituicao, UUID id);

    Page<Conta> findBySituacao(Situacao situacao, Pageable pageable);

    List<Conta> findBySituacao(Situacao situacao);

    List<Conta> findBySituacaoAndIncluirNoTotal(Situacao situacao, Boolean incluirNoTotal);

    @Query("""
            SELECT SUM(conta.saldo)
            FROM Conta conta
            WHERE conta.situacao = br.com.prismaapi.enums.Situacao.ATIVO
              AND conta.incluirNoTotal = TRUE
            """)
    BigDecimal somarSaldoDoTotal();
}