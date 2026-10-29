package br.com.prismaapi.controller.aviso;

import br.com.prismaapi.config.AbstractControllerTest;
import br.com.prismaapi.service.aviso.AvisoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
class AvisoControllerTest extends AbstractControllerTest {

    private String registrarAdiamentoAvisoRequest;
    private String atualizarPreferenciasAvisosRequest;

    @Autowired
    private AvisoService avisoService;

    @BeforeEach
    void setUp() throws IOException {
        if (registrarAdiamentoAvisoRequest == null) {
            registrarAdiamentoAvisoRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/aviso/registrarAdiamentoAvisoRequest.json")));
        }

        if (atualizarPreferenciasAvisosRequest == null) {
            atualizarPreferenciasAvisosRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/aviso/atualizarPreferenciasAvisosRequest.json")));
        }
    }

    @Test
    void listarAvisosTest() throws Exception {
        testGet("/v1/avisos");
    }

    @Test
    void registrarLeituraAvisoTest() throws Exception {
        var idAviso = avisoService.listar().getFirst().id();

        testPost("/v1/avisos/" + idAviso + "/leitura", "");
    }

    @Test
    void registrarAdiamentoAvisoTest() throws Exception {
        var idAviso = avisoService.listar().getFirst().id();

        testPost("/v1/avisos/" + idAviso + "/adiamento", registrarAdiamentoAvisoRequest);
    }

    @Test
    void deletarLeituraAvisoTest() throws Exception {
        var idAviso = avisoService.listar().getFirst().id();

        avisoService.registrarLeitura(idAviso);

        testDelete("/v1/avisos/" + idAviso + "/leitura");
    }

    @Test
    void registrarLeituraAvisosTest() throws Exception {
        testPost("/v1/avisos/leitura", "");
    }

    @Test
    void deletarLeiturasAvisosTest() throws Exception {
        testDelete("/v1/avisos/leitura");
    }

    @Test
    void buscarPreferenciasAvisosTest() throws Exception {
        testGet("/v1/avisos/preferencias");
    }

    @Test
    void atualizarPreferenciasAvisosTest() throws Exception {
        testPut("/v1/avisos/preferencias", atualizarPreferenciasAvisosRequest);
    }
}