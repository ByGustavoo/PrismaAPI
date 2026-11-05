package br.com.prismaapi.controller.recorrente;

import br.com.prismaapi.config.AbstractControllerTest;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.recorrente.RecorrenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
class RecorrenteControllerTest extends AbstractControllerTest {

    @Autowired
    private ContaRepository contaRepository;

    private String salvarRecorrenteRequest;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private String atualizarRecorrenteRequest;

    @Autowired
    private RecorrenteRepository recorrenteRepository;

    @BeforeEach
    void setUp() throws IOException {
        if (salvarRecorrenteRequest == null) {
            salvarRecorrenteRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/recorrente/salvarRecorrenteRequest.json")));
        }

        if (atualizarRecorrenteRequest == null) {
            atualizarRecorrenteRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/recorrente/atualizarRecorrenteRequest.json")));
        }
    }

    @Test
    void listarRecorrentesTest() throws Exception {
        testGet("/v1/recorrentes");
    }

    @Test
    void salvarRecorrenteTest() throws Exception {
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Saúde")).getId();
        var idOrigem = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();

        testPost("/v1/recorrentes", salvarRecorrenteRequest.formatted(idCategoria, idOrigem));
    }

    @Test
    void atualizarRecorrenteTest() throws Exception {
        var idRecorrente = buscar(recorrenteRepository, recorrente -> recorrente.getDescricao().equals("Aluguel")).getId();
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Moradia")).getId();
        var idOrigem = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();

        testPut("/v1/recorrentes/" + idRecorrente, atualizarRecorrenteRequest.formatted(idCategoria, idOrigem));
    }

    @Test
    void deletarRecorrenteTest() throws Exception {
        var idRecorrente = buscar(recorrenteRepository, recorrente -> recorrente.getDescricao().equals("Revista digital")).getId();

        testDelete("/v1/recorrentes/" + idRecorrente);
    }
}