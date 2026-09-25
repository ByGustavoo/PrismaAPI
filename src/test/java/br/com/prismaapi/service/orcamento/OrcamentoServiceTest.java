package br.com.prismaapi.service.orcamento;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.model.dto.orcamento.SalvarOrcamentoDTO;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.orcamento.OrcamentoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.YearMonth;

@SpringBootTest
class OrcamentoServiceTest extends AbstractTest {

    @Autowired
    private OrcamentoService orcamentoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @Test
    void visaoGeralTest() {
        var visaoGeral = Assertions.assertDoesNotThrow(() -> orcamentoService.visaoGeral(YearMonth.now()));
        Assertions.assertNotNull(visaoGeral);
    }

    @Test
    void salvarTest() {
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Moradia")).getId();

        var salvarOrcamentoDTO = new SalvarOrcamentoDTO(idCategoria, new BigDecimal("2500.00"));

        var orcamento = Assertions.assertDoesNotThrow(() -> orcamentoService.salvar(salvarOrcamentoDTO));
        Assertions.assertNotNull(orcamento);
    }

    @Test
    void atualizarTest() {
        var idOrcamento = buscar(orcamentoRepository, orcamento -> orcamento.getCategoria().getNome().equals("Alimentação")).getId();
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Alimentação")).getId();

        var salvarOrcamentoDTO = new SalvarOrcamentoDTO(idCategoria, new BigDecimal("2100.00"));

        var orcamento = Assertions.assertDoesNotThrow(() -> orcamentoService.atualizar(idOrcamento, salvarOrcamentoDTO));
        Assertions.assertNotNull(orcamento);
    }

    @Test
    void deletarTest() {
        var idOrcamento = buscar(orcamentoRepository, orcamento -> orcamento.getCategoria().getNome().equals("Lazer")).getId();

        Assertions.assertDoesNotThrow(() -> orcamentoService.deletar(idOrcamento));
    }
}