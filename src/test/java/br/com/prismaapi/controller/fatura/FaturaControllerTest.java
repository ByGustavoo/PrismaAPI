package br.com.prismaapi.controller.fatura;

import br.com.prismaapi.config.AbstractControllerTest;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.YearMonth;

@SpringBootTest
class FaturaControllerTest extends AbstractControllerTest {

    @Autowired
    private CartaoRepository cartaoRepository;

    @Test
    void listarFaturasTest() throws Exception {
        testGet("/v1/faturas");
    }

    @Test
    void buscarFaturaTest() throws Exception {
        var idCartao = buscar(cartaoRepository, cartao -> cartao.getNome().equals("Aurora Platinum")).getId();

        testGet("/v1/faturas/" + idCartao + "-" + YearMonth.now());
    }
}