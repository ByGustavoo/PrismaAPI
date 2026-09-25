package br.com.prismaapi.controller.meta;

import br.com.prismaapi.config.AbstractControllerTest;
import br.com.prismaapi.repository.meta.MetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

@SpringBootTest
class MetaControllerTest extends AbstractControllerTest {

    private String salvarMetaRequest;
    private String atualizarMetaRequest;
    private String registrarPrecoRequest;

    @Autowired
    private MetaRepository metaRepository;

    @BeforeEach
    void setUp() throws IOException {
        if (salvarMetaRequest == null) {
            salvarMetaRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/meta/salvarMetaRequest.json")));
        }

        if (atualizarMetaRequest == null) {
            atualizarMetaRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/meta/atualizarMetaRequest.json")));
        }

        if (registrarPrecoRequest == null) {
            registrarPrecoRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/meta/registrarPrecoRequest.json")));
        }
    }

    @Test
    void listarMetasTest() throws Exception {
        testGet("/v1/metas");
    }

    @Test
    void salvarMetaTest() throws Exception {
        testPost("/v1/metas", salvarMetaRequest);
    }

    @Test
    void atualizarMetaTest() throws Exception {
        var idMeta = buscar(metaRepository, meta -> meta.getNome().equals("Cadeira ergonômica")).getId();

        testPut("/v1/metas/" + idMeta, atualizarMetaRequest);
    }

    @Test
    void registrarPrecoTest() throws Exception {
        var idMeta = buscar(metaRepository, meta -> meta.getNome().equals("Notebook para edição")).getId();

        testPost("/v1/metas/" + idMeta + "/precos", registrarPrecoRequest.formatted(LocalDate.now()));
    }

    @Test
    void deletarMetaTest() throws Exception {
        var idMeta = buscar(metaRepository, meta -> meta.getNome().equals("Console de videogame")).getId();

        testDelete("/v1/metas/" + idMeta);
    }
}