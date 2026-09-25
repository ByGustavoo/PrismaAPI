package br.com.prismaapi.controller.dashboard;

import br.com.prismaapi.config.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DashboardControllerTest extends AbstractControllerTest {

    @Test
    void buscarResumoTest() throws Exception {
        testGet("/v1/dashboard/resumo");
    }
}