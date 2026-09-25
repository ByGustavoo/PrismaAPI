package br.com.prismaapi.service.cartao;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.model.dto.cartao.SalvarCartaoDTO;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class CartaoServiceTest extends AbstractTest {

    @Autowired
    private CartaoService cartaoService;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Test
    void listarTest() {
        var cartoes = Assertions.assertDoesNotThrow(() -> cartaoService.listar());
        Assertions.assertNotNull(cartoes);
    }

    @Test
    void salvarTest() {
        var salvarCartaoDTO = new SalvarCartaoDTO(
                "Aurora Black",
                "Banco Aurora",
                TipoCartao.CREDITO,
                Situacao.ATIVO,
                "Mastercard",
                "5566",
                new BigDecimal("8000.00"),
                (short) 5,
                (short) 12,
                null,
                null);

        var cartao = Assertions.assertDoesNotThrow(() -> cartaoService.salvar(salvarCartaoDTO));
        Assertions.assertNotNull(cartao);
    }

    @Test
    void atualizarTest() {
        var idCartao = buscar(cartaoRepository, cartao -> cartao.getNome().equals("Cartão antigo")).getId();

        var salvarCartaoDTO = new SalvarCartaoDTO(
                "Cartão antigo",
                "Banco Horizonte",
                TipoCartao.CREDITO,
                Situacao.INATIVO,
                "Visa",
                "0055",
                new BigDecimal("2500.00"),
                (short) 10,
                (short) 17,
                null,
                null);

        var cartao = Assertions.assertDoesNotThrow(() -> cartaoService.atualizar(idCartao, salvarCartaoDTO));
        Assertions.assertNotNull(cartao);
    }

    @Test
    void deletarTest() {
        var idCartao = buscar(cartaoRepository, cartao -> cartao.getNome().equals("Cartão antigo")).getId();

        Assertions.assertDoesNotThrow(() -> cartaoService.deletar(idCartao));
    }
}