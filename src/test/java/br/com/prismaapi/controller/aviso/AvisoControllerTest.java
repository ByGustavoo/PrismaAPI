package br.com.prismaapi.controller.aviso;

import br.com.prismaapi.config.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AvisoControllerTest extends AbstractControllerTest {

    @Test
    void listarAvisosTest() throws Exception {
        testGet("/v1/avisos");
    }
}