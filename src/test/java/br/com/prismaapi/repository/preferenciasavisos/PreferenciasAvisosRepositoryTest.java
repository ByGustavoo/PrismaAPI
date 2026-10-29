package br.com.prismaapi.repository.preferenciasavisos;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PreferenciasAvisosRepositoryTest extends AbstractTest {

    @Autowired
    private PreferenciasAvisosRepository preferenciasAvisosRepository;

    @Test
    void findFirstByOrderByDataAtualizacaoAscTest() {
        Assertions.assertDoesNotThrow(() -> preferenciasAvisosRepository.findFirstByOrderByDataAtualizacaoAsc());
    }
}