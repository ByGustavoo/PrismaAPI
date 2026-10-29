package br.com.prismaapi.controller.tela;

import br.com.prismaapi.config.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
class TelaControllerTest extends AbstractControllerTest {

    private String atualizarTelasOcultasRequest;

    @BeforeEach
    void setUp() throws IOException {
        if (atualizarTelasOcultasRequest == null) {
            atualizarTelasOcultasRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/tela/atualizarTelasOcultasRequest.json")));
        }
    }

    @Test
    void buscarTelasOcultasTest() throws Exception {
        testGet("/v1/telas/ocultas");
    }

    @Test
    void atualizarTelasOcultasTest() throws Exception {
        testPut("/v1/telas/ocultas", atualizarTelasOcultasRequest);
    }
}