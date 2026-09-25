package br.com.prismaapi.service.despesarecorrente;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.Frequencia;
import br.com.prismaapi.enums.SituacaoDespesaRecorrente;
import br.com.prismaapi.model.dto.despesarecorrente.SalvarDespesaRecorrenteDTO;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.despesarecorrente.DespesaRecorrenteRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
class DespesaRecorrenteServiceTest extends AbstractTest {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private DespesaRecorrenteService despesaRecorrenteService;

    @Autowired
    private DespesaRecorrenteRepository despesaRecorrenteRepository;

    @Test
    void resumirTest() {
        var resumo = Assertions.assertDoesNotThrow(() -> despesaRecorrenteService.resumir());
        Assertions.assertNotNull(resumo);
    }

    @Test
    void salvarTest() {
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Saúde")).getId();
        var idOrigem = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();

        var salvarDespesaRecorrenteDTO = new SalvarDespesaRecorrenteDTO(
                "Seguro de vida",
                new BigDecimal("89.90"),
                idCategoria,
                Frequencia.MENSAL,
                LocalDate.of(2026, 10, 5),
                idOrigem,
                SituacaoDespesaRecorrente.ATIVO,
                "Débito automático.");

        var despesa = Assertions.assertDoesNotThrow(() -> despesaRecorrenteService.salvar(salvarDespesaRecorrenteDTO));
        Assertions.assertNotNull(despesa);
    }

    @Test
    void atualizarTest() {
        var idDespesa = buscar(despesaRecorrenteRepository, despesa -> despesa.getDescricao().equals("Aluguel")).getId();
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Moradia")).getId();
        var idOrigem = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();

        var salvarDespesaRecorrenteDTO = new SalvarDespesaRecorrenteDTO(
                "Aluguel",
                new BigDecimal("2350.00"),
                idCategoria,
                Frequencia.MENSAL,
                LocalDate.of(2026, 10, 10),
                idOrigem,
                SituacaoDespesaRecorrente.ATIVO,
                "Reajuste anual pelo IPCA.");

        var despesa = Assertions.assertDoesNotThrow(() -> despesaRecorrenteService.atualizar(idDespesa, salvarDespesaRecorrenteDTO));
        Assertions.assertNotNull(despesa);
    }

    @Test
    void deletarTest() {
        var idDespesa = buscar(despesaRecorrenteRepository, despesa -> despesa.getDescricao().equals("Revista digital")).getId();

        Assertions.assertDoesNotThrow(() -> despesaRecorrenteService.deletar(idDespesa));
    }
}