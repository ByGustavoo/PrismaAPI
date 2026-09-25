package br.com.prismaapi.service.relatorio;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
class RelatorioServiceTest extends AbstractTest {

    @Autowired
    private RelatorioService relatorioService;

    @Test
    void resumirTest() {
        var relatorio = Assertions.assertDoesNotThrow(() -> relatorioService.resumir(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30)));
        Assertions.assertNotNull(relatorio);
    }
}