package br.com.prismaapi.repository.meta;

import br.com.prismaapi.enums.SituacaoMeta;
import br.com.prismaapi.model.entity.meta.Meta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MetaRepository extends JpaRepository<Meta, UUID> {

    Page<Meta> findBySituacao(SituacaoMeta situacao, Pageable pageable);

}
