package br.com.prismaapi.repository.conta;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.Situacao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class ContaRepositoryTest extends AbstractTest {

    @Autowired
    private ContaRepository contaRepository;

    @Test
    void existsByNomeIgnoreCaseAndInstituicaoIgnoreCaseTest() {
        Assertions.assertDoesNotThrow(() -> contaRepository.existsByNomeIgnoreCaseAndInstituicaoIgnoreCase("Conta principal", "Banco Aurora"));
    }

    @Test
    void existsByNomeIgnoreCaseAndInstituicaoIgnoreCaseAndIdNotTest() {
        Assertions.assertDoesNotThrow(() -> contaRepository.existsByNomeIgnoreCaseAndInstituicaoIgnoreCaseAndIdNot("Conta principal", "Banco Aurora", UUID.randomUUID()));
    }

    @Test
    void findBySituacaoTest() {
        Assertions.assertDoesNotThrow(() -> contaRepository.findBySituacao(Situacao.ATIVO));
    }

    @Test
    void somarSaldoDoTotalTest() {
        Assertions.assertDoesNotThrow(() -> contaRepository.somarSaldoDoTotal());
    }
}