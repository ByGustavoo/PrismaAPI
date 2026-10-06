package br.com.prismaapi.service.fatura;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.compraparcelada.CompraParceladaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
        var idCartao = buscar(cartaoRepository, cartao -> cartao.getNome().equals("Aurora Platinum")).getId();

        var fatura = Assertions.assertDoesNotThrow(() -> faturaService.registrarPagamento(idCartao + "-" + YearMonth.now()));
        Assertions.assertNotNull(fatura);
    }

    @Test
    void deletarPagamentoTest() {
        var idCartao = buscar(cartaoRepository, cartao -> cartao.getNome().equals("Aurora Platinum")).getId();

        faturaService.registrarPagamento(idCartao + "-" + YearMonth.now());

        Assertions.assertDoesNotThrow(() -> faturaService.deletarPagamento(idCartao + "-" + YearMonth.now()));
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
}