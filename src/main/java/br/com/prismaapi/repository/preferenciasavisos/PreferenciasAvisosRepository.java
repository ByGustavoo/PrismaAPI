package br.com.prismaapi.repository.preferenciasavisos;

import br.com.prismaapi.model.entity.preferenciasavisos.PreferenciasAvisos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PreferenciasAvisosRepository extends JpaRepository<PreferenciasAvisos, UUID> {

    @Query("""
            SELECT preferencias
            FROM PreferenciasAvisos preferencias
            LEFT JOIN FETCH preferencias.tiposDesligados
            ORDER BY preferencias.dataAtualizacao ASC
            """)
    List<PreferenciasAvisos> buscarComTiposDesligados();
}