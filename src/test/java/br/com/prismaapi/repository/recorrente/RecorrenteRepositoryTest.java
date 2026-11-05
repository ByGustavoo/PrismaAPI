package br.com.prismaapi.repository.recorrente;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.SituacaoRecorrente;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class RecorrenteRepositoryTest extends AbstractTest {

    @Autowired
    private RecorrenteRepository recorrenteRepository;

    @Test
    void countByContaIdTest() {
        Assertions.assertDoesNotThrow(() -> recorrenteRepository.countByContaId(UUID.randomUUID()));
    }

    @Test
    void countByCartaoIdTest() {
        Assertions.assertDoesNotThrow(() -> recorrenteRepository.countByCartaoId(UUID.randomUUID()));
    }

    @Test
    void findBySituacaoTest() {
        Assertions.assertDoesNotThrow(() -> recorrenteRepository.findBySituacao(SituacaoRecorrente.ATIVO));
    }

    @Test
    void buscarComOrigemECategoriaTest() {
        Assertions.assertDoesNotThrow(() -> recorrenteRepository.buscarComOrigemECategoria());
    }
}