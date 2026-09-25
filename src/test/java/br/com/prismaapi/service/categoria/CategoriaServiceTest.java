package br.com.prismaapi.service.categoria;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.TipoCategoria;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CategoriaServiceTest extends AbstractTest {

    @Autowired
    private CategoriaService categoriaService;

    @Test
    void listarTest() {
        var categorias = Assertions.assertDoesNotThrow(() -> categoriaService.listar(TipoCategoria.DESPESA));
        Assertions.assertNotNull(categorias);
    }
}