package br.com.prismaapi.service.recorrente;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.Frequencia;
import br.com.prismaapi.enums.SituacaoRecorrente;
import br.com.prismaapi.enums.TipoRecorrente;
import br.com.prismaapi.model.dto.recorrente.SalvarRecorrenteDTO;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.repository.recorrente.RecorrenteRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
class RecorrenteServiceTest extends AbstractTest {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private RecorrenteService recorrenteService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Autowired
    private RecorrenteRepository recorrenteRepository;

    @Test
    void resumirTest() {
        var resumo = Assertions.assertDoesNotThrow(() -> recorrenteService.resumir());
        Assertions.assertNotNull(resumo);
    }

    @Test
    void salvarTest() {
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Saúde")).getId();
        var idOrigem = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();

        var salvarRecorrenteDTO = new SalvarRecorrenteDTO(
                TipoRecorrente.DESPESA,
                "Seguro de vida",
                new BigDecimal("89.90"),
                idCategoria,
                Frequencia.MENSAL,
                LocalDate.of(2026, 10, 5),
                idOrigem,
                SituacaoRecorrente.ATIVO,
                true,
                "Débito automático.");

        var despesa = Assertions.assertDoesNotThrow(() -> recorrenteService.salvar(salvarRecorrenteDTO));
        Assertions.assertNotNull(despesa);
    }

    @Test
    void atualizarTest() {
        var idRecorrente = buscar(recorrenteRepository, recorrente -> recorrente.getDescricao().equals("Aluguel")).getId();
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Moradia")).getId();
        var idOrigem = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();

        var salvarRecorrenteDTO = new SalvarRecorrenteDTO(
                TipoRecorrente.DESPESA,
                "Aluguel",
                new BigDecimal("2350.00"),
                idCategoria,
                Frequencia.MENSAL,
                LocalDate.of(2026, 10, 10),
                idOrigem,
                SituacaoRecorrente.ATIVO,
                false,
                "Reajuste anual pelo IPCA.");

        var despesa = Assertions.assertDoesNotThrow(() -> recorrenteService.atualizar(idRecorrente, salvarRecorrenteDTO));
        Assertions.assertNotNull(despesa);
    }

    @Test
    void repetirTest() {
        var lancamento = buscar(lancamentoRepository, salvo -> salvo.getDescricao().equals("Aluguel"));

        var recorrente = Assertions.assertDoesNotThrow(() -> recorrenteService.repetir(lancamento, Frequencia.MENSAL));
        Assertions.assertNotNull(recorrente);
    }

    @Test
    void deletarTest() {
        var idRecorrente = buscar(recorrenteRepository, recorrente -> recorrente.getDescricao().equals("Revista digital")).getId();

        Assertions.assertDoesNotThrow(() -> recorrenteService.deletar(idRecorrente));
    }
}