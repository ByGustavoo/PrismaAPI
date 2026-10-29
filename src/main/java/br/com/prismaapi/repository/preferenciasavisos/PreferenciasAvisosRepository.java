package br.com.prismaapi.repository.preferenciasavisos;

import br.com.prismaapi.model.entity.preferenciasavisos.PreferenciasAvisos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PreferenciasAvisosRepository extends JpaRepository<PreferenciasAvisos, UUID> {

    Optional<PreferenciasAvisos> findFirstByOrderByDataAtualizacaoAsc();
}