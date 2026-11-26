package br.com.prismaapi.repository.lancamento;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.DirecaoOrdenacao;
import br.com.prismaapi.enums.OrdenacaoLancamento;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.dto.lancamento.FiltroLancamentoDTO;
import br.com.prismaapi.repository.conta.ContaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class LancamentoRepositoryTest extends AbstractTest {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Test
    void findAllTest() {
        var filtro = new FiltroLancamentoDTO(TipoLancamento.DESPESA, "Mercado", LocalDate.now().minusYears(1), LocalDate.now(), null, null, SituacaoLancamento.PAGO, 0, 25, OrdenacaoLancamento.DATA, DirecaoOrdenacao.DESC);

        Assertions.assertDoesNotThrow(() -> lancamentoRepository.findAll(LancamentoSpecification.filtrar(filtro), PageRequest.of(0, 25, Sort.by(Sort.Order.desc("data")))));
    }

    @Test
    void somarResultadoTest() {
        var filtro = new FiltroLancamentoDTO(null, "reajuste", LocalDate.now().minusYears(1), LocalDate.now(), null, null, null, 0, 25, OrdenacaoLancamento.DATA, DirecaoOrdenacao.DESC);

        Assertions.assertDoesNotThrow(() -> lancamentoRepository.somarResultado(LancamentoSpecification.filtrar(filtro)));
    }

    @Test
    void findByParcelaPagaIdInTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.findByParcelaPagaIdIn(List.of(UUID.randomUUID())));
    }

    @Test
    void countByContaIdOrContaDestinoIdOrContaPagamentoFaturaIdTest() {
        var id = UUID.randomUUID();

        Assertions.assertDoesNotThrow(() -> lancamentoRepository.countByContaIdOrContaDestinoIdOrContaPagamentoFaturaId(id, id, id));
    }

    @Test
    void countByCartaoIdTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.countByCartaoId(UUID.randomUUID()));
    }

    @Test
    void buscarCompletoTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.buscarCompleto(UUID.randomUUID()));
    }

    @Test
    void resumirPeriodosTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.resumirPeriodos(LocalDate.now().minusMonths(1), LocalDate.now(), LocalDate.now().minusMonths(2), LocalDate.now().minusMonths(1).minusDays(1)));
    }

    @Test
    void agruparReceitasEDespesasPorDiaTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparReceitasEDespesasPorDia(LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    @Test
    void agruparPorCategoriaTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparPorCategoria(TipoLancamento.RECEITA, LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    @Test
    void agruparDespesasPorOrigemTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparDespesasPorOrigem(LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    @Test
    void agruparMovimentosDoTotalPorDiaTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparMovimentosDoTotalPorDia(LocalDate.now().minusYears(1), LocalDate.now().plusMonths(1), LocalDate.now()));
    }

    @Test
    void somarDespesasEmAbertoDoCartaoTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.somarDespesasEmAbertoDoCartao(UUID.randomUUID(), LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    @Test
    void agruparPagamentosDeFaturaDoTotalPorDiaTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparPagamentosDeFaturaDoTotalPorDia(LocalDate.now().minusYears(1), LocalDate.now()));
    }

    @Test
    void marcarDespesasDoCartaoComoPagasTest() {
        var conta = buscar(contaRepository, encontrada -> encontrada.getNome().equals("Conta principal"));

        Assertions.assertDoesNotThrow(() -> lancamentoRepository.marcarDespesasDoCartaoComoPagas(UUID.randomUUID(), LocalDate.now().minusMonths(1), LocalDate.now(), LocalDate.now(), conta));
    }

    @Test
    void desmarcarDespesasDoCartaoTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.desmarcarDespesasDoCartao(UUID.randomUUID(), LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    @Test
    void agruparDespesasDosCartoesTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparDespesasDosCartoes(List.of(UUID.randomUUID()), LocalDate.now().minusMonths(3)));
    }

    @Test
    void buscarDespesasDoCartaoTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.buscarDespesasDoCartao(UUID.randomUUID(), LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    @Test
    void buscarRecentesTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.buscarRecentes(LocalDate.now().minusMonths(1), LocalDate.now(), PageRequest.of(0, 5)));
    }

    @Test
    void buscarNaoPagosAteTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.buscarNaoPagosAte(LocalDate.now().plusDays(15)));
    }

    @Test
    void buscarComOrigemEntreTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.buscarComOrigemEntre(LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(6)));
    }

    @Test
    void findByMovimentacaoInvestimentoInvestimentoIdTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.findByMovimentacaoInvestimentoInvestimentoId(UUID.randomUUID()));
    }
}