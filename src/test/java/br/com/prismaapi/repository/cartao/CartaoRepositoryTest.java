package br.com.prismaapi.repository.cartao;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoCartao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class CartaoRepositoryTest extends AbstractTest {

    @Autowired
    private CartaoRepository cartaoRepository;

    @Test
    void countByContaIdTest() {
        Assertions.assertDoesNotThrow(() -> cartaoRepository.countByContaId(UUID.randomUUID()));
    }

    @Test
    void findByTipoTest() {
        Assertions.assertDoesNotThrow(() -> cartaoRepository.findByTipo(TipoCartao.CREDITO));
    }

    @Test
    void findBySituacaoAndTipoNotTest() {
        Assertions.assertDoesNotThrow(() -> cartaoRepository.findBySituacaoAndTipoNot(Situacao.ATIVO, TipoCartao.DEBITO));
    }

    @Test
    void buscarComContaTest() {
        Assertions.assertDoesNotThrow(() -> cartaoRepository.buscarComConta());
    }
}