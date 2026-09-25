package br.com.prismaapi.service.investimento;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.ClasseAtivo;
import br.com.prismaapi.model.dto.investimento.AtualizarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarAporteInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarSaldoInvestimentoDTO;
import br.com.prismaapi.repository.investimento.InvestimentoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@SpringBootTest
class InvestimentoServiceTest extends AbstractTest {

    @Autowired
    private InvestimentoService investimentoService;

    @Autowired
    private InvestimentoRepository investimentoRepository;

    @Test
    void resumirCarteiraTest() {
        var carteira = Assertions.assertDoesNotThrow(() -> investimentoService.resumirCarteira());
        Assertions.assertNotNull(carteira);
    }

    @Test
    void evolucaoNosMesesTest() {
        var evolucao = Assertions.assertDoesNotThrow(() -> investimentoService.evolucaoNosMeses(List.of(YearMonth.now().minusMonths(1), YearMonth.now())));
        Assertions.assertNotNull(evolucao);
    }

    @Test
    void buscarExtratoTest() {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("CDB 110% do CDI")).getId();

        var extrato = Assertions.assertDoesNotThrow(() -> investimentoService.buscarExtrato(idInvestimento));
        Assertions.assertNotNull(extrato);
    }

    @Test
    void salvarTest() {
        var salvarInvestimentoDTO = new SalvarInvestimentoDTO(
                "Tesouro IPCA+ 2035",
                ClasseAtivo.TESOURO,
                "Corretora Prisma",
                new BigDecimal("3000.00"),
                new BigDecimal("3000.00"),
                LocalDate.of(2026, 9, 1),
                "Aposentadoria.");

        var investimento = Assertions.assertDoesNotThrow(() -> investimentoService.salvar(salvarInvestimentoDTO));
        Assertions.assertNotNull(investimento);
    }

    @Test
    void atualizarTest() {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("Bitcoin")).getId();

        var atualizarInvestimentoDTO = new AtualizarInvestimentoDTO(
                "Bitcoin",
                ClasseAtivo.CRIPTO,
                "Corretora Cripto Norte",
                "Posição de longo prazo.");

        var investimento = Assertions.assertDoesNotThrow(() -> investimentoService.atualizar(idInvestimento, atualizarInvestimentoDTO));
        Assertions.assertNotNull(investimento);
    }

    @Test
    void registrarAporteTest() {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("Tesouro Selic 2029")).getId();

        var salvarAporteInvestimentoDTO = new SalvarAporteInvestimentoDTO(
                new BigDecimal("500.00"),
                LocalDate.now(),
                "Aporte mensal");

        var investimento = Assertions.assertDoesNotThrow(() -> investimentoService.registrarAporte(idInvestimento, salvarAporteInvestimentoDTO));
        Assertions.assertNotNull(investimento);
    }

    @Test
    void registrarSaldoTest() {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("Tesouro Selic 2029")).getId();

        var salvarSaldoInvestimentoDTO = new SalvarSaldoInvestimentoDTO(
                new BigDecimal("12500.00"),
                LocalDate.now(),
                "Extrato do mês");

        var investimento = Assertions.assertDoesNotThrow(() -> investimentoService.registrarSaldo(idInvestimento, salvarSaldoInvestimentoDTO));
        Assertions.assertNotNull(investimento);
    }

    @Test
    void deletarTest() {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("LCI pós-fixada")).getId();

        Assertions.assertDoesNotThrow(() -> investimentoService.deletar(idInvestimento));
    }
}