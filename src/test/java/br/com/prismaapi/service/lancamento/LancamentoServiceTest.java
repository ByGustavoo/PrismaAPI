package br.com.prismaapi.service.lancamento;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.FormaLancamento;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.dto.lancamento.FiltroLancamentoDTO;
import br.com.prismaapi.model.dto.lancamento.SalvarLancamentoDTO;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
class LancamentoServiceTest extends AbstractTest {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private LancamentoService lancamentoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Test
    void listarTest() {
        var filtroLancamentoDTO = new FiltroLancamentoDTO(
                TipoLancamento.DESPESA,
                null,
                LocalDate.now().minusMonths(1),
                LocalDate.now(),
                null,
                null,
                SituacaoLancamento.PAGO);

        var lancamentos = Assertions.assertDoesNotThrow(() -> lancamentoService.listar(filtroLancamentoDTO));
        Assertions.assertNotNull(lancamentos);
    }

    @Test
    void salvarTest() {
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Alimentação")).getId();
        var idOrigem = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();

        var salvarLancamentoDTO = new SalvarLancamentoDTO(
                "Supermercado",
                new BigDecimal("245.80"),
                TipoLancamento.DESPESA,
                SituacaoLancamento.PAGO,
                FormaLancamento.PIX,
                LocalDate.of(2026, 9, 1),
                idCategoria,
                idOrigem,
                null,
                "Compra do mês.");

        var lancamento = Assertions.assertDoesNotThrow(() -> lancamentoService.salvar(salvarLancamentoDTO));
        Assertions.assertNotNull(lancamento);
    }

    @Test
    void atualizarTest() {
        var idLancamento = buscar(lancamentoRepository, lancamento -> lancamento.getDescricao().equals("Aluguel")).getId();
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Moradia")).getId();
        var idOrigem = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();

        var salvarLancamentoDTO = new SalvarLancamentoDTO(
                "Aluguel",
                new BigDecimal("2350.00"),
                TipoLancamento.DESPESA,
                SituacaoLancamento.PAGO,
                FormaLancamento.PIX,
                LocalDate.of(2026, 9, 10),
                idCategoria,
                idOrigem,
                null,
                "Reajuste anual pelo IPCA.");

        var lancamento = Assertions.assertDoesNotThrow(() -> lancamentoService.atualizar(idLancamento, salvarLancamentoDTO));
        Assertions.assertNotNull(lancamento);
    }

    @Test
    void deletarTest() {
        var idLancamento = buscar(lancamentoRepository, lancamento -> lancamento.getDescricao().equals("Aluguel")).getId();

        Assertions.assertDoesNotThrow(() -> lancamentoService.deletar(idLancamento));
    }
}