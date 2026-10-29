package br.com.prismaapi.repository.leituraaviso;

import br.com.prismaapi.model.entity.leituraaviso.LeituraAviso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeituraAvisoRepository extends JpaRepository<LeituraAviso, UUID> {

    Optional<LeituraAviso> findByChave(String chave);

    @Modifying
    @Query("""
            DELETE FROM LeituraAviso leitura
            WHERE leitura.chave NOT IN :chaves
            """)
    void deletarForaDe(@Param("chaves") Collection<String> chaves);
}