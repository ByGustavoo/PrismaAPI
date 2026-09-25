package br.com.prismaapi.controller.compraparcelada;

import br.com.prismaapi.config.AbstractControllerTest;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.compraparcelada.CompraParceladaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
class CompraParceladaControllerTest extends AbstractControllerTest {

    @Autowired
    private CartaoRepository cartaoRepository;

    private String salvarCompraParceladaRequest;
    private String atualizarCompraParceladaRequest;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CompraParceladaRepository compraParceladaRepository;

    @BeforeEach
    void setUp() throws IOException {
        if (salvarCompraParceladaRequest == null) {
            salvarCompraParceladaRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/compraparcelada/salvarCompraParceladaRequest.json")));
        }

        if (atualizarCompraParceladaRequest == null) {
            atualizarCompraParceladaRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/requests/compraparcelada/atualizarCompraParceladaRequest.json")));
        }
    }

    @Test
    void listarComprasParceladasTest() throws Exception {
        testGet("/v1/compras-parceladas");
    }

    @Test
    void salvarCompraParceladaTest() throws Exception {
        var idCartao = buscar(cartaoRepository, cartao -> cartao.getNome().equals("Aurora Platinum")).getId();
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Compras")).getId();

        testPost("/v1/compras-parceladas", salvarCompraParceladaRequest.formatted(idCartao, idCategoria));
    }

    @Test
    void atualizarCompraParceladaTest() throws Exception {
        var idCompra = buscar(compraParceladaRepository, compra -> compra.getDescricao().equals("Geladeira")).getId();
        var idCartao = buscar(cartaoRepository, cartao -> cartao.getNome().equals("Horizonte Gold")).getId();
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Casa")).getId();

        testPut("/v1/compras-parceladas/" + idCompra, atualizarCompraParceladaRequest.formatted(idCartao, idCategoria));
    }

    @Test
    void deletarCompraParceladaTest() throws Exception {
        var idCompra = buscar(compraParceladaRepository, compra -> compra.getDescricao().equals("Sofá")).getId();

        testDelete("/v1/compras-parceladas/" + idCompra);
    }
}