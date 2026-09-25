package br.com.prismaapi.service.previsao;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PrevisaoServiceTest extends AbstractTest {

    @Autowired
    private PrevisaoService previsaoService;

    @Test
    void preverTest() {
        var previsao = Assertions.assertDoesNotThrow(() -> previsaoService.prever(6));
        Assertions.assertNotNull(previsao);
    }
}