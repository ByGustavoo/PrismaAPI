package br.com.prismaapi.repository.cartao;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.model.entity.cartao.Cartao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartaoRepository extends JpaRepository<Cartao, UUID> {

    boolean existsByContaId(UUID contaId);

    Page<Cartao> findByTipoAndSituacao(TipoCartao tipo, Situacao situacao, Pageable pageable);

    List<Cartao> findByContaId(UUID contaId);

}
