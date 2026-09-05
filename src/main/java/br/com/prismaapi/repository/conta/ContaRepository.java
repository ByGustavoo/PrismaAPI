package br.com.prismaapi.repository.conta;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.model.entity.conta.Conta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContaRepository extends JpaRepository<Conta, UUID> {

    boolean existsByNomeAndInstituicao(String nome, String instituicao);

    boolean existsByNomeAndInstituicaoAndIdNot(String nome, String instituicao, UUID id);

    Page<Conta> findBySituacao(Situacao situacao, Pageable pageable);

    List<Conta> findBySituacaoAndIncluirNoTotal(Situacao situacao, Boolean incluirNoTotal);

}
