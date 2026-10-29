package br.com.prismaapi.controller.investimento;

import br.com.prismaapi.config.AbstractControllerTest;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.investimento.InvestimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

@SpringBootTest
class InvestimentoControllerTest extends AbstractControllerTest {

    private String registrarSaldoRequest;
    private String registrarAporteRequest;
    private String registrarResgateRequest;
    private String salvarInvestimentoRequest;
    private String atualizarInvestimentoRequest;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private InvestimentoRepository investimentoRepository;

    @BeforeEach
    void setUp() throws IOException {
        if (salvarInvestimentoRequest == null) {
            salvarInvestimentoRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/investimento/salvarInvestimentoRequest.json")));
        }

        if (atualizarInvestimentoRequest == null) {
            atualizarInvestimentoRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/investimento/atualizarInvestimentoRequest.json")));
        }

        if (registrarAporteRequest == null) {
            registrarAporteRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/investimento/registrarAporteRequest.json")));
        }

        if (registrarResgateRequest == null) {
            registrarResgateRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/investimento/registrarResgateRequest.json")));
        }

        if (registrarSaldoRequest == null) {
            registrarSaldoRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/investimento/registrarSaldoRequest.json")));
        }
    }

    @Test
    void buscarCarteiraTest() throws Exception {
        testGet("/v1/investimentos/carteira");
    }

    @Test
    void buscarExtratoTest() throws Exception {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("CDB 110% do CDI")).getId();

        testGet("/v1/investimentos/" + idInvestimento + "/extrato");
    }

    @Test
    void salvarInvestimentoTest() throws Exception {
        testPost("/v1/investimentos", salvarInvestimentoRequest);
    }

    @Test
    void atualizarInvestimentoTest() throws Exception {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("Bitcoin")).getId();

        testPut("/v1/investimentos/" + idInvestimento, atualizarInvestimentoRequest);
    }

    @Test
    void registrarAporteTest() throws Exception {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("Tesouro Selic 2029")).getId();

        testPost("/v1/investimentos/" + idInvestimento + "/aportes", registrarAporteRequest.formatted(LocalDate.now()));
    }

    @Test
    void registrarResgateTest() throws Exception {
        var idConta = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("Tesouro Selic 2029")).getId();

        testPost("/v1/investimentos/" + idInvestimento + "/resgates", registrarResgateRequest.formatted(LocalDate.now(), idConta));
    }

    @Test
    void registrarSaldoTest() throws Exception {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("Tesouro Selic 2029")).getId();

        testPost("/v1/investimentos/" + idInvestimento + "/saldos", registrarSaldoRequest.formatted(LocalDate.now()));
    }

    @Test
    void deletarInvestimentoTest() throws Exception {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("LCI pós-fixada")).getId();

        testDelete("/v1/investimentos/" + idInvestimento);
    }
}