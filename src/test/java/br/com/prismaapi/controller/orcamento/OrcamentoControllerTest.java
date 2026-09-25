package br.com.prismaapi.controller.orcamento;

import br.com.prismaapi.config.AbstractControllerTest;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.orcamento.OrcamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
class OrcamentoControllerTest extends AbstractControllerTest {

    private String salvarOrcamentoRequest;
    private String atualizarOrcamentoRequest;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @BeforeEach
    void setUp() throws IOException {
        if (salvarOrcamentoRequest == null) {
            salvarOrcamentoRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/orcamento/salvarOrcamentoRequest.json")));
        }

        if (atualizarOrcamentoRequest == null) {
            atualizarOrcamentoRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/orcamento/atualizarOrcamentoRequest.json")));
        }
    }

    @Test
    void buscarVisaoGeralTest() throws Exception {
        testGet("/v1/orcamentos/visao-geral");
    }

    @Test
    void salvarOrcamentoTest() throws Exception {
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Moradia")).getId();

        testPost("/v1/orcamentos", salvarOrcamentoRequest.formatted(idCategoria));
    }

    @Test
    void atualizarOrcamentoTest() throws Exception {
        var idOrcamento = buscar(orcamentoRepository, orcamento -> orcamento.getCategoria().getNome().equals("Alimentação")).getId();
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Alimentação")).getId();

        testPut("/v1/orcamentos/" + idOrcamento, atualizarOrcamentoRequest.formatted(idCategoria));
    }

    @Test
    void deletarOrcamentoTest() throws Exception {
        var idOrcamento = buscar(orcamentoRepository, orcamento -> orcamento.getCategoria().getNome().equals("Lazer")).getId();

        testDelete("/v1/orcamentos/" + idOrcamento);
    }
}