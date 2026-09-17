package br.com.prismaapi.repository.compraparcelada;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class CompraParceladaRepositoryTest extends AbstractTest {

    @Autowired
    private CompraParceladaRepository compraParceladaRepository;

    @Test
    void countByCartaoIdTest() {
        Assertions.assertDoesNotThrow(() -> compraParceladaRepository.countByCartaoId(UUID.randomUUID()));
    }

    @Test
    void buscarParcelasAteTest() {
        Assertions.assertDoesNotThrow(() -> compraParceladaRepository.buscarParcelasAte(LocalDate.now().withDayOfMonth(1)));
    }

    @Test
    void buscarParcelasDosCartoesTest() {
        Assertions.assertDoesNotThrow(() -> compraParceladaRepository.buscarParcelasDosCartoes(List.of(UUID.randomUUID())));
    }

    @Test
    void buscarDoCartaoAteTest() {
        Assertions.assertDoesNotThrow(() -> compraParceladaRepository.buscarDoCartaoAte(UUID.randomUUID(), LocalDate.now().withDayOfMonth(1)));
    }

    @Test
    void buscarComCartaoECategoriaTest() {
        Assertions.assertDoesNotThrow(() -> compraParceladaRepository.buscarComCartaoECategoria());
    }

    @Test
    void buscarComCartaoTest() {
        Assertions.assertDoesNotThrow(() -> compraParceladaRepository.buscarComCartao());
    }
}