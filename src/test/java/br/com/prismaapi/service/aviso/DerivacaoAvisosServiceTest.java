package br.com.prismaapi.service.aviso;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
class DerivacaoAvisosServiceTest extends AbstractTest {

    @Autowired
    private DerivacaoAvisosService derivacaoAvisosService;

    @Test
    void derivarTest() {
        var avisos = Assertions.assertDoesNotThrow(() -> derivacaoAvisosService.derivar(LocalDate.now()));
        Assertions.assertNotNull(avisos);
    }
}