package br.com.prismaapi.repository.categoria;

import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.model.entity.categoria.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    boolean existsByNomeAndTipo(String nome, TipoCategoria tipo);

    boolean existsByNomeAndTipoAndIdNot(String nome, TipoCategoria tipo, UUID id);

    Page<Categoria> findByTipo(TipoCategoria tipo, Pageable pageable);

}
