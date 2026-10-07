package br.com.prismaapi.controller.fatura;

import br.com.prismaapi.config.AbstractControllerTest;
import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.model.entity.cartao.Cartao;
import br.com.prismaapi.model.entity.compraparcelada.CompraParcelada;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.compraparcelada.CompraParceladaRepository;
import br.com.prismaapi.service.fatura.FaturaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@SpringBootTest
class FaturaControllerTest extends AbstractControllerTest {

    @Autowired
    private FaturaService faturaService;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private CompraParceladaRepository compraParceladaRepository;

    @Test
    void listarFaturasTest() throws Exception {
        testGet("/v1/faturas");
    }

    @Test
    void buscarFaturaTest() throws Exception {
        var idCartao = buscar(cartaoRepository, cartao -> cartao.getNome().equals("Aurora Platinum")).getId();

        testGet("/v1/faturas/" + idCartao + "-" + YearMonth.now());
    }

    @Test
    void registrarPagamentoFaturaTest() throws Exception {
        var idFatura = faturaEmAberto();

        testPost("/v1/faturas/" + idFatura + "/pagamento", "");
    }

    @Test
    void deletarPagamentoFaturaTest() throws Exception {
        var idFatura = faturaEmAberto();

        faturaService.registrarPagamento(idFatura);

        testDelete("/v1/faturas/" + idFatura + "/pagamento");
    }

    private String faturaEmAberto() {
        var cartao = new Cartao();

        cartao.setNome("Cartão sem pagamento");
        cartao.setInstituicao("Banco Aurora");
        cartao.setTipo(TipoCartao.CREDITO);
        cartao.setSituacao(Situacao.ATIVO);
        cartao.setLimiteCredito(new BigDecimal("3000.00"));
        cartao.setDiaFechamento((short) 3);
        cartao.setDiaVencimento((short) 10);

        var compra = new CompraParcelada();

        compra.setDescricao("Compra em aberto");
        compra.setValorTotal(new BigDecimal("600.00"));
        compra.setParcelas((short) 2);
        compra.setDataCompra(LocalDate.now());
        compra.setPrimeiroMes(YearMonth.now().atDay(1));
        compra.setCartao(cartaoRepository.save(cartao));

        compraParceladaRepository.save(compra);

        return cartao.getId() + "-" + YearMonth.now();
    }
}