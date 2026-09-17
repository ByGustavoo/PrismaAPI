package br.com.prismaapi.repository.despesarecorrente;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.SituacaoDespesaRecorrente;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class DespesaRecorrenteRepositoryTest extends AbstractTest {

    @Autowired
    private DespesaRecorrenteRepository despesaRecorrenteRepository;

    @Test
    void countByContaIdTest() {
        Assertions.assertDoesNotThrow(() -> despesaRecorrenteRepository.countByContaId(UUID.randomUUID()));
    }

    @Test
    void countByCartaoIdTest() {
        Assertions.assertDoesNotThrow(() -> despesaRecorrenteRepository.countByCartaoId(UUID.randomUUID()));
    }

    @Test
    void findBySituacaoTest() {
        Assertions.assertDoesNotThrow(() -> despesaRecorrenteRepository.findBySituacao(SituacaoDespesaRecorrente.ATIVO));
    }

    @Test
    void buscarComOrigemECategoriaTest() {
        Assertions.assertDoesNotThrow(() -> despesaRecorrenteRepository.buscarComOrigemECategoria());
    }
}