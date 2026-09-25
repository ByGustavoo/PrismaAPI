package br.com.prismaapi.controller.sistema;

import br.com.prismaapi.config.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SistemaControllerTest extends AbstractControllerTest {

    @Test
    void buscarVersaoTest() throws Exception {
        testGet("/v1/sistema/versao");
    }
}