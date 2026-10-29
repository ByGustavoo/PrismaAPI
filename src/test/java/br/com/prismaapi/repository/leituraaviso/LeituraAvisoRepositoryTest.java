package br.com.prismaapi.repository.leituraaviso;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class LeituraAvisoRepositoryTest extends AbstractTest {

    @Autowired
    private LeituraAvisoRepository leituraAvisoRepository;

    @Test
    void findByChaveTest() {
        Assertions.assertDoesNotThrow(() -> leituraAvisoRepository.findByChave("aviso-saldo-negativo-previsto"));
    }

    @Test
    void deletarForaDeTest() {
        Assertions.assertDoesNotThrow(() -> leituraAvisoRepository.deletarForaDe(List.of("aviso-saldo-negativo-previsto")));
    }
}