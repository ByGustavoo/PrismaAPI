package br.com.prismaapi.repository.movimentacaoinvestimento;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class MovimentacaoInvestimentoRepositoryTest extends AbstractTest {

    @Autowired
    private MovimentacaoInvestimentoRepository movimentacaoInvestimentoRepository;

    @Test
    void findByInvestimentoIdTest() {
        Assertions.assertDoesNotThrow(() -> movimentacaoInvestimentoRepository.findByInvestimentoId(UUID.randomUUID()));
    }
}