package br.com.prismaapi.controller.categoria;

import br.com.prismaapi.config.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CategoriaControllerTest extends AbstractControllerTest {

    @Test
    void listarCategoriasTest() throws Exception {
        testGet("/v1/categorias");
    }
}