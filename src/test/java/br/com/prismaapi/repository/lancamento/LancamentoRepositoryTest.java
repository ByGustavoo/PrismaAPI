package br.com.prismaapi.repository.lancamento;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.dto.lancamento.FiltroLancamentoDTO;
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
    private LancamentoRepository lancamentoRepository;

    @Test
    void findAllTest() {
        var filtro = new FiltroLancamentoDTO(TipoLancamento.DESPESA, "Mercado", LocalDate.now().minusYears(1), LocalDate.now(), null, null, SituacaoLancamento.PAGO);

        Assertions.assertDoesNotThrow(() -> lancamentoRepository.findAll(LancamentoSpecification.filtrar(filtro), Sort.by(Sort.Order.desc("data"))));
    }

    @Test
    void countByContaIdOrContaDestinoIdTest() {
        var id = UUID.randomUUID();

        Assertions.assertDoesNotThrow(() -> lancamentoRepository.countByContaIdOrContaDestinoId(id, id));
    }

    @Test
    void countByCartaoIdTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.countByCartaoId(UUID.randomUUID()));
    }

    @Test
    void countByTipoNotAndDataBetweenTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.countByTipoNotAndDataBetween(TipoLancamento.TRANSFERENCIA, LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    @Test
    void somarPorTipoTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.somarPorTipo(TipoLancamento.RECEITA, LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    @Test
    void agruparTotaisPorMesTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparTotaisPorMes(LocalDate.now().minusYears(1), LocalDate.now()));
    }

    @Test
    void agruparReceitasEDespesasPorDiaTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparReceitasEDespesasPorDia(LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    @Test
    void agruparDespesasPorDiaTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparDespesasPorDia(LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    @Test
    void agruparDespesasPorCategoriaTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparDespesasPorCategoria(LocalDate.now().minusMonths(1), LocalDate.now()));
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
    void agruparMovimentoDoTotalPorDiaTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparMovimentoDoTotalPorDia(LocalDate.now().minusYears(1), LocalDate.now().plusMonths(1), LocalDate.now()));
    }

    @Test
    void agruparDespesasNoCreditoPorDiaTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparDespesasNoCreditoPorDia(LocalDate.now().minusYears(1), LocalDate.now()));
    }

    @Test
    void agruparTransferenciasQueSaemDoTotalTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparTransferenciasQueSaemDoTotal(LocalDate.now().minusYears(1), LocalDate.now().plusMonths(1), LocalDate.now()));
    }

    @Test
    void agruparTransferenciasQueEntramNoTotalTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparTransferenciasQueEntramNoTotal(LocalDate.now().minusYears(1), LocalDate.now().plusMonths(1), LocalDate.now()));
    }

    @Test
    void somarDespesasDoCartaoTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.somarDespesasDoCartao(UUID.randomUUID(), LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    @Test
    void agruparDespesasDosCartoesTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.agruparDespesasDosCartoes(List.of(UUID.randomUUID())));
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
    void buscarPagosDasContasTest() {
        Assertions.assertDoesNotThrow(() -> lancamentoRepository.buscarPagosDasContas(List.of(UUID.randomUUID()), LocalDate.now().minusYears(1), LocalDate.now()));
    }
}