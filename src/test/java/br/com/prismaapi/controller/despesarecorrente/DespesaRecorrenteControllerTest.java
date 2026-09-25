package br.com.prismaapi.controller.despesarecorrente;

import br.com.prismaapi.config.AbstractControllerTest;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.despesarecorrente.DespesaRecorrenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
class DespesaRecorrenteControllerTest extends AbstractControllerTest {

    @Autowired
    private ContaRepository contaRepository;

    private String salvarDespesaRecorrenteRequest;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private String atualizarDespesaRecorrenteRequest;

    @Autowired
    private DespesaRecorrenteRepository despesaRecorrenteRepository;

    @BeforeEach
    void setUp() throws IOException {
        if (salvarDespesaRecorrenteRequest == null) {
            salvarDespesaRecorrenteRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/despesarecorrente/salvarDespesaRecorrenteRequest.json")));
        }

        if (atualizarDespesaRecorrenteRequest == null) {
            atualizarDespesaRecorrenteRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/despesarecorrente/atualizarDespesaRecorrenteRequest.json")));
        }
    }

    @Test
    void listarDespesasRecorrentesTest() throws Exception {
        testGet("/v1/despesas-recorrentes");
    }

    @Test
    void salvarDespesaRecorrenteTest() throws Exception {
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Saúde")).getId();
        var idOrigem = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();

        testPost("/v1/despesas-recorrentes", salvarDespesaRecorrenteRequest.formatted(idCategoria, idOrigem));
    }

    @Test
    void atualizarDespesaRecorrenteTest() throws Exception {
        var idDespesa = buscar(despesaRecorrenteRepository, despesa -> despesa.getDescricao().equals("Aluguel")).getId();
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Moradia")).getId();
        var idOrigem = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();

        testPut("/v1/despesas-recorrentes/" + idDespesa, atualizarDespesaRecorrenteRequest.formatted(idCategoria, idOrigem));
    }

    @Test
    void deletarDespesaRecorrenteTest() throws Exception {
        var idDespesa = buscar(despesaRecorrenteRepository, despesa -> despesa.getDescricao().equals("Revista digital")).getId();

        testDelete("/v1/despesas-recorrentes/" + idDespesa);
    }
}