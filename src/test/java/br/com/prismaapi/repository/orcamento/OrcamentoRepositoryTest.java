package br.com.prismaapi.repository.orcamento;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class OrcamentoRepositoryTest extends AbstractTest {

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @Test
    void existsByCategoriaIdTest() {
        Assertions.assertDoesNotThrow(() -> orcamentoRepository.existsByCategoriaId(UUID.randomUUID()));
    }

    @Test
    void existsByCategoriaIdAndIdNotTest() {
        Assertions.assertDoesNotThrow(() -> orcamentoRepository.existsByCategoriaIdAndIdNot(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void buscarComCategoriaTest() {
        Assertions.assertDoesNotThrow(() -> orcamentoRepository.buscarComCategoria());
    }
}