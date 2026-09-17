package br.com.prismaapi.repository.metapreco;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class MetaPrecoRepositoryTest extends AbstractTest {

    @Autowired
    private MetaPrecoRepository metaPrecoRepository;

    @Test
    void existsByMetaIdAndDataAndPrecoTest() {
        Assertions.assertDoesNotThrow(() -> metaPrecoRepository.existsByMetaIdAndDataAndPreco(UUID.randomUUID(), LocalDate.now(), new BigDecimal("7849.00")));
    }

    @Test
    void findByMetaIdOrderByDataAscDataCriacaoAscTest() {
        Assertions.assertDoesNotThrow(() -> metaPrecoRepository.findByMetaIdOrderByDataAscDataCriacaoAsc(UUID.randomUUID()));
    }

    @Test
    void findByMetaIdInOrderByDataAscDataCriacaoAscTest() {
        Assertions.assertDoesNotThrow(() -> metaPrecoRepository.findByMetaIdInOrderByDataAscDataCriacaoAsc(List.of(UUID.randomUUID())));
    }
}