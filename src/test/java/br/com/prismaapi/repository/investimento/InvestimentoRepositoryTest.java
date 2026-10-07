package br.com.prismaapi.repository.investimento;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
class InvestimentoRepositoryTest extends AbstractTest {

    @Autowired
    private InvestimentoRepository investimentoRepository;

    @Test
    void findByDataUltimaMovimentacaoLessThanEqualTest() {
        Assertions.assertDoesNotThrow(() -> investimentoRepository.findByDataUltimaMovimentacaoLessThanEqual(LocalDate.now().minusDays(30)));
    }

    @Test
    void resumirCarteiraTest() {
        Assertions.assertDoesNotThrow(() -> investimentoRepository.resumirCarteira());
    }
}