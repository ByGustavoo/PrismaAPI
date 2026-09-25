package br.com.prismaapi.service.conta;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoConta;
import br.com.prismaapi.model.dto.conta.SalvarContaDTO;
import br.com.prismaapi.model.entity.conta.Conta;
import br.com.prismaapi.repository.conta.ContaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class ContaServiceTest extends AbstractTest {

    @Autowired
    private ContaService contaService;

    @Autowired
    private ContaRepository contaRepository;

    @Test
    void listarTest() {
        var contas = Assertions.assertDoesNotThrow(() -> contaService.listar());
        Assertions.assertNotNull(contas);
    }

    @Test
    void listarOrigensTest() {
        var origens = Assertions.assertDoesNotThrow(() -> contaService.listarOrigens());
        Assertions.assertNotNull(origens);
    }

    @Test
    void salvarTest() {
        var salvarContaDTO = new SalvarContaDTO(
                "Conta investimento",
                "Banco Horizonte",
                TipoConta.CORRENTE,
                new BigDecimal("1500.00"),
                Situacao.ATIVO,
                true);

        var conta = Assertions.assertDoesNotThrow(() -> contaService.salvar(salvarContaDTO));
        Assertions.assertNotNull(conta);
    }

    @Test
    void atualizarTest() {
        var idConta = buscar(contaRepository, conta -> conta.getNome().equals("Carteira")).getId();

        var salvarContaDTO = new SalvarContaDTO(
                "Carteira",
                "Dinheiro vivo",
                TipoConta.OUTRA,
                new BigDecimal("350.00"),
                Situacao.ATIVO,
                true);

        var conta = Assertions.assertDoesNotThrow(() -> contaService.atualizar(idConta, salvarContaDTO));
        Assertions.assertNotNull(conta);
    }

    @Test
    void deletarTest() {
        var conta = new Conta();

        conta.setNome("Conta sem histórico");
        conta.setInstituicao("Banco Aurora");
        conta.setTipo(TipoConta.CORRENTE);
        conta.setSaldo(BigDecimal.ZERO);
        conta.setSituacao(Situacao.ATIVO);
        conta.setIncluirNoTotal(true);

        var idConta = contaRepository.save(conta).getId();

        Assertions.assertDoesNotThrow(() -> contaService.deletar(idConta));
    }
}