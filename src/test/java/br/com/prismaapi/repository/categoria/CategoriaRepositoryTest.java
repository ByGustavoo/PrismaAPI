package br.com.prismaapi.repository.categoria;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.TipoCategoria;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CategoriaRepositoryTest extends AbstractTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void findByTipoTest() {
        Assertions.assertDoesNotThrow(() -> categoriaRepository.findByTipo(TipoCategoria.DESPESA));
    }
}