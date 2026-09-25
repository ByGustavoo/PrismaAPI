package br.com.prismaapi.service.aviso;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AvisoServiceTest extends AbstractTest {

    @Autowired
    private AvisoService avisoService;

    @Test
    void listarTest() {
        var avisos = Assertions.assertDoesNotThrow(() -> avisoService.listar());
        Assertions.assertNotNull(avisos);
    }
}