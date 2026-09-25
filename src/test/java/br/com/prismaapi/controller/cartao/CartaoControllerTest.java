package br.com.prismaapi.controller.cartao;

import br.com.prismaapi.config.AbstractControllerTest;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
class CartaoControllerTest extends AbstractControllerTest {

    private String salvarCartaoRequest;
    private String atualizarCartaoRequest;

    @Autowired
    private CartaoRepository cartaoRepository;

    @BeforeEach
    void setUp() throws IOException {
        if (salvarCartaoRequest == null) {
            salvarCartaoRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/cartao/salvarCartaoRequest.json")));
        }

        if (atualizarCartaoRequest == null) {
            atualizarCartaoRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/cartao/atualizarCartaoRequest.json")));
        }
    }

    @Test
    void listarCartoesTest() throws Exception {
        testGet("/v1/cartoes");
    }

    @Test
    void salvarCartaoTest() throws Exception {
        testPost("/v1/cartoes", salvarCartaoRequest);
    }

    @Test
    void atualizarCartaoTest() throws Exception {
        var idCartao = buscar(cartaoRepository, cartao -> cartao.getNome().equals("Cartão antigo")).getId();

        testPut("/v1/cartoes/" + idCartao, atualizarCartaoRequest);
    }

    @Test
    void deletarCartaoTest() throws Exception {
        var idCartao = buscar(cartaoRepository, cartao -> cartao.getNome().equals("Cartão antigo")).getId();

        testDelete("/v1/cartoes/" + idCartao);
    }
}