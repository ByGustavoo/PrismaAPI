package br.com.prismaapi.controller.relatorio;

import br.com.prismaapi.config.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RelatorioControllerTest extends AbstractControllerTest {

    @Test
    void buscarResumoTest() throws Exception {
        testGet("/v1/relatorios/resumo?dataInicial=2026-01-01&dataFinal=2026-06-30");
    }
}