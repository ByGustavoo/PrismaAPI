package br.com.prismaapi.repository.meta;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.SituacaoMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MetaRepositoryTest extends AbstractTest {

    @Autowired
    private MetaRepository metaRepository;

    @Test
    void findAllTest() {
        Assertions.assertDoesNotThrow(() -> metaRepository.findAll(MetaSpecification.filtrar(SituacaoMeta.ACOMPANHANDO, "Notebook")));
    }
}