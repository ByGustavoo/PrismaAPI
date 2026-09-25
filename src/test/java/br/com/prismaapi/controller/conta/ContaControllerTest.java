package br.com.prismaapi.controller.conta;

import br.com.prismaapi.config.AbstractControllerTest;
import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoConta;
import br.com.prismaapi.model.entity.conta.Conta;
import br.com.prismaapi.repository.conta.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
class ContaControllerTest extends AbstractControllerTest {

    private String salvarContaRequest;
    private String atualizarContaRequest;

    @Autowired
    private ContaRepository contaRepository;

    @BeforeEach
    void setUp() throws IOException {
        if (salvarContaRequest == null) {
            salvarContaRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/conta/salvarContaRequest.json")));
        }

        if (atualizarContaRequest == null) {
            atualizarContaRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/conta/atualizarContaRequest.json")));
        }
    }

    @Test
    void listarContasTest() throws Exception {
        testGet("/v1/contas");
    }

    @Test
    void listarOrigensTest() throws Exception {
        testGet("/v1/contas/origens");
    }

    @Test
    void listarReservasTest() throws Exception {
        testGet("/v1/contas/reservas");
    }

    @Test
    void buscarEvolucaoTest() throws Exception {
        var idConta = buscar(contaRepository, conta -> conta.getNome().equals("Reserva de emergência")).getId();

        testGet("/v1/contas/" + idConta + "/evolucao");
    }

    @Test
    void salvarContaTest() throws Exception {
        testPost("/v1/contas", salvarContaRequest);
    }

    @Test
    void atualizarContaTest() throws Exception {
        var idConta = buscar(contaRepository, conta -> conta.getNome().equals("Carteira")).getId();

        testPut("/v1/contas/" + idConta, atualizarContaRequest);
    }

    @Test
    void deletarContaTest() throws Exception {
        var conta = new Conta();

        conta.setNome("Conta sem histórico");
        conta.setInstituicao("Banco Aurora");
        conta.setTipo(TipoConta.CORRENTE);
        conta.setSaldo(BigDecimal.ZERO);
        conta.setSituacao(Situacao.ATIVO);
        conta.setIncluirNoTotal(true);

        testDelete("/v1/contas/" + contaRepository.save(conta).getId());
    }
}