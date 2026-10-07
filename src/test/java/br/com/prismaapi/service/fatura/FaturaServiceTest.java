package br.com.prismaapi.service.fatura;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.model.entity.cartao.Cartao;
import br.com.prismaapi.model.entity.compraparcelada.CompraParcelada;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.compraparcelada.CompraParceladaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@SpringBootTest
class FaturaServiceTest extends AbstractTest {

    @Autowired
    private FaturaService faturaService;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private CompraParceladaRepository compraParceladaRepository;

    @Test
    void listarTest() {
        var faturas = Assertions.assertDoesNotThrow(() -> faturaService.listar(null));
        Assertions.assertNotNull(faturas);
    }

    @Test
    void detalharTest() {
        var idCartao = buscar(cartaoRepository, cartao -> cartao.getNome().equals("Aurora Platinum")).getId();

        var fatura = Assertions.assertDoesNotThrow(() -> faturaService.detalhar(idCartao + "-" + YearMonth.now()));
        Assertions.assertNotNull(fatura);
    }

    @Test
    void registrarPagamentoTest() {
        var idFatura = faturaEmAberto();

        var fatura = Assertions.assertDoesNotThrow(() -> faturaService.registrarPagamento(idFatura));
        Assertions.assertNotNull(fatura);
    }

    @Test
    void deletarPagamentoTest() {
        var idFatura = faturaEmAberto();

        faturaService.registrarPagamento(idFatura);

        Assertions.assertDoesNotThrow(() -> faturaService.deletarPagamento(idFatura));
    }

    @Test
    void faturaEmDestaqueTest() {
        var fatura = Assertions.assertDoesNotThrow(() -> faturaService.faturaEmDestaque(YearMonth.now(), LocalDate.now()));
        Assertions.assertNotNull(fatura);
    }

    @Test
    void limitesComprometidosTest() {
        var limites = Assertions.assertDoesNotThrow(() -> faturaService.limitesComprometidos(cartaoRepository.findAll(), LocalDate.now()));
        Assertions.assertNotNull(limites);
    }

    @Test
    void cronogramaTest() {
        var compra = buscar(compraParceladaRepository, encontrada -> encontrada.getDescricao().equals("Notebook"));

        var parcelas = Assertions.assertDoesNotThrow(() -> faturaService.cronograma(compra, LocalDate.now(), Map.of()));
        Assertions.assertNotNull(parcelas);
    }

    @Test
    void pagamentosPorCompraTest() {
        var pagamentos = Assertions.assertDoesNotThrow(() -> faturaService.pagamentosPorCompra());
        Assertions.assertNotNull(pagamentos);
    }

    @Test
    void parcelasPorDataDePagamentoTest() {
        var parcelas = Assertions.assertDoesNotThrow(() -> faturaService.parcelasPorDataDePagamento(LocalDate.now()));
        Assertions.assertNotNull(parcelas);
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