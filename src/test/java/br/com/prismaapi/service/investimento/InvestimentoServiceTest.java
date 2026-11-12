package br.com.prismaapi.service.investimento;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.ClasseAtivo;
import br.com.prismaapi.model.dto.investimento.AtualizarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarAcaoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarAporteInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarCompraAcaoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarCotacaoAcaoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarResgateInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarSaldoInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarVendaAcaoDTO;
import br.com.prismaapi.repository.conta.ContaRepository;
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
    private ContaRepository contaRepository;

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
                false,
                null,
                "Aposentadoria.");

        var investimento = Assertions.assertDoesNotThrow(() -> investimentoService.salvar(salvarInvestimentoDTO));
        Assertions.assertNotNull(investimento);
    }

    @Test
    void salvarAcaoTest() {
        var salvarAcaoDTO = new SalvarAcaoDTO(
                "itub4",
                "Itaú Unibanco",
                "Corretora Prisma",
                new BigDecimal("50"),
                new BigDecimal("34.80"),
                new BigDecimal("2.50"),
                new BigDecimal("35.10"),
                LocalDate.now().minusDays(5),
                false,
                null,
                "Bancos pagam bons dividendos.");

        var investimento = Assertions.assertDoesNotThrow(() -> investimentoService.salvarAcao(salvarAcaoDTO));
        Assertions.assertNotNull(investimento);
    }

    @Test
    void atualizarTest() {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("Bitcoin")).getId();

        var atualizarInvestimentoDTO = new AtualizarInvestimentoDTO(
                "Bitcoin",
                null,
                ClasseAtivo.CRIPTO,
                "Corretora Cripto Norte",
                false,
                null,
                "Posição de longo prazo.");

        var investimento = Assertions.assertDoesNotThrow(() -> investimentoService.atualizar(idInvestimento, atualizarInvestimentoDTO));
        Assertions.assertNotNull(investimento);
    }

    @Test
    void registrarAporteTest() {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("Tesouro Selic 2029")).getId();

        var idConta = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();

        var salvarAporteInvestimentoDTO = new SalvarAporteInvestimentoDTO(
                new BigDecimal("500.00"),
                LocalDate.now(),
                idConta,
                "Aporte mensal");

        var investimento = Assertions.assertDoesNotThrow(() -> investimentoService.registrarAporte(idInvestimento, salvarAporteInvestimentoDTO));
        Assertions.assertNotNull(investimento);
    }

    @Test
    void registrarResgateTest() {
        var idConta = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("Tesouro Selic 2029")).getId();

        var salvarResgateInvestimentoDTO = new SalvarResgateInvestimentoDTO(
                new BigDecimal("1000.00"),
                LocalDate.now(),
                idConta,
                "Conserto do carro");

        var investimento = Assertions.assertDoesNotThrow(() -> investimentoService.registrarResgate(idInvestimento, salvarResgateInvestimentoDTO));
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
    void registrarCompraTest() {
        var idConta = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("Petrobras")).getId();

        var salvarCompraAcaoDTO = new SalvarCompraAcaoDTO(
                new BigDecimal("20"),
                new BigDecimal("37.40"),
                new BigDecimal("1.90"),
                LocalDate.now(),
                idConta,
                "Aproveitando a queda");

        var investimento = Assertions.assertDoesNotThrow(() -> investimentoService.registrarCompra(idInvestimento, salvarCompraAcaoDTO));
        Assertions.assertNotNull(investimento);
    }

    @Test
    void registrarVendaTest() {
        var idConta = buscar(contaRepository, conta -> conta.getNome().equals("Conta principal")).getId();
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("Petrobras")).getId();

        var salvarVendaAcaoDTO = new SalvarVendaAcaoDTO(
                new BigDecimal("30"),
                new BigDecimal("38.00"),
                new BigDecimal("1.90"),
                LocalDate.now(),
                idConta,
                "Realizando lucro");

        var investimento = Assertions.assertDoesNotThrow(() -> investimentoService.registrarVenda(idInvestimento, salvarVendaAcaoDTO));
        Assertions.assertNotNull(investimento);
    }

    @Test
    void registrarCotacaoTest() {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("Petrobras")).getId();

        var salvarCotacaoAcaoDTO = new SalvarCotacaoAcaoDTO(
                new BigDecimal("36.75"),
                LocalDate.now(),
                "Home broker");

        var investimento = Assertions.assertDoesNotThrow(() -> investimentoService.registrarCotacao(idInvestimento, salvarCotacaoAcaoDTO));
        Assertions.assertNotNull(investimento);
    }

    @Test
    void deletarTest() {
        var idInvestimento = buscar(investimentoRepository, investimento -> investimento.getNome().equals("LCI pós-fixada")).getId();

        Assertions.assertDoesNotThrow(() -> investimentoService.deletar(idInvestimento));
    }
}