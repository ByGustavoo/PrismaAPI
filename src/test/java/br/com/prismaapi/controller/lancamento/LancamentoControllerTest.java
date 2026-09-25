package br.com.prismaapi.controller.lancamento;

import br.com.prismaapi.config.AbstractControllerTest;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
class LancamentoControllerTest extends AbstractControllerTest {

    private String salvarLancamentoRequest;

    @Autowired
    private ContaRepository contaRepository;

    private String atualizarLancamentoRequest;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @BeforeEach
    void setUp() throws IOException {
        if (salvarLancamentoRequest == null) {
            salvarLancamentoRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/lancamento/salvarLancamentoRequest.json")));
        }

        if (atualizarLancamentoRequest == null) {
            atualizarLancamentoRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/lancamento/atualizarLancamentoRequest.json")));
        }
    }

    @Test
    void listarLancamentosTest() throws Exception {
        testGet("/v1/lancamentos");
    }

    @Test
    void salvarLancamentoTest() throws Exception {
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Alimentação")).getId();
        var idOrigem = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();

        testPost("/v1/lancamentos", salvarLancamentoRequest.formatted(idCategoria, idOrigem));
    }

    @Test
    void atualizarLancamentoTest() throws Exception {
        var idLancamento = buscar(lancamentoRepository, lancamento -> lancamento.getDescricao().equals("Aluguel")).getId();
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Moradia")).getId();
        var idOrigem = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();

        testPut("/v1/lancamentos/" + idLancamento, atualizarLancamentoRequest.formatted(idCategoria, idOrigem));
    }

    @Test
    void deletarLancamentoTest() throws Exception {
        var idLancamento = buscar(lancamentoRepository, lancamento -> lancamento.getDescricao().equals("Aluguel")).getId();

        testDelete("/v1/lancamentos/" + idLancamento);
    }
}