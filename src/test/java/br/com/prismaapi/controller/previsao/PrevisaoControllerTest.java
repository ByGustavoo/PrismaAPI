package br.com.prismaapi.controller.previsao;

import br.com.prismaapi.config.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PrevisaoControllerTest extends AbstractControllerTest {

    @Test
    void buscarPrevisaoTest() throws Exception {
        testGet("/v1/previsao");
    }
}