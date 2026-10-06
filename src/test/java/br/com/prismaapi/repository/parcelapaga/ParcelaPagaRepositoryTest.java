package br.com.prismaapi.repository.parcelapaga;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class ParcelaPagaRepositoryTest extends AbstractTest {

    @Autowired
    private ParcelaPagaRepository parcelaPagaRepository;

    @Test
    void findByCompraParceladaIdAndNumeroTest() {
        Assertions.assertDoesNotThrow(() -> parcelaPagaRepository.findByCompraParceladaIdAndNumero(UUID.randomUUID(), (short) 1));
    }

    @Test
    void buscarPagamentosTest() {
        Assertions.assertDoesNotThrow(() -> parcelaPagaRepository.buscarPagamentos());
    }

    @Test
    void deletarAcimaDeTest() {
        Assertions.assertDoesNotThrow(() -> parcelaPagaRepository.deletarAcimaDe(UUID.randomUUID(), (short) 3));
    }
}