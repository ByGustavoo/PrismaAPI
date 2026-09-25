package br.com.prismaapi.service.sistema;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SistemaServiceTest extends AbstractTest {

    @Autowired
    private SistemaService sistemaService;

    @Test
    void buscarVersaoTest() {
        var versao = Assertions.assertDoesNotThrow(() -> sistemaService.buscarVersao());
        Assertions.assertNotNull(versao);
    }
}