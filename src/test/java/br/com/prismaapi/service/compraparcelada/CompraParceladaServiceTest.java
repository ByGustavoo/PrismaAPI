package br.com.prismaapi.service.compraparcelada;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.model.dto.compraparcelada.SalvarCompraParceladaDTO;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.compraparcelada.CompraParceladaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@SpringBootTest
class CompraParceladaServiceTest extends AbstractTest {

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CompraParceladaService compraParceladaService;

    @Autowired
    private CompraParceladaRepository compraParceladaRepository;

    @Test
    void listarTest() {
        var compras = Assertions.assertDoesNotThrow(() -> compraParceladaService.listar(null));
        Assertions.assertNotNull(compras);
    }

    @Test
    void salvarTest() {
        var idCartao = buscar(cartaoRepository, cartao -> cartao.getNome().equals("Aurora Platinum")).getId();
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Compras")).getId();

        var salvarCompraParceladaDTO = new SalvarCompraParceladaDTO(
                "Smartphone",
                new BigDecimal("3200.00"),
                (short) 10,
                LocalDate.of(2026, 9, 15),
                YearMonth.of(2026, 10),
                idCartao,
                idCategoria,
                "Troca do aparelho antigo.");

        var compra = Assertions.assertDoesNotThrow(() -> compraParceladaService.salvar(salvarCompraParceladaDTO));
        Assertions.assertNotNull(compra);
    }

    @Test
    void atualizarTest() {
        var idCompra = buscar(compraParceladaRepository, compra -> compra.getDescricao().equals("Geladeira")).getId();
        var idCartao = buscar(cartaoRepository, cartao -> cartao.getNome().equals("Horizonte Gold")).getId();
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Casa")).getId();

        var salvarCompraParceladaDTO = new SalvarCompraParceladaDTO(
                "Geladeira",
                new BigDecimal("3800.00"),
                (short) 12,
                LocalDate.of(2026, 1, 21),
                YearMonth.of(2026, 2),
                idCartao,
                idCategoria,
                "Frete incluído.");

        var compra = Assertions.assertDoesNotThrow(() -> compraParceladaService.atualizar(idCompra, salvarCompraParceladaDTO));
        Assertions.assertNotNull(compra);
    }

    @Test
    void deletarTest() {
        var idCompra = buscar(compraParceladaRepository, compra -> compra.getDescricao().equals("Sofá")).getId();

        Assertions.assertDoesNotThrow(() -> compraParceladaService.deletar(idCompra));
    }
}