package br.com.prismaapi.service.tela;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.Tela;
import br.com.prismaapi.model.dto.tela.TelasOcultasDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class TelaServiceTest extends AbstractTest {

    @Autowired
    private TelaService telaService;

    @Test
    void buscarOcultasTest() {
        var telas = Assertions.assertDoesNotThrow(() -> telaService.buscarOcultas());
        Assertions.assertNotNull(telas);
    }

    @Test
    void atualizarOcultasTest() {
        var telasOcultasDTO = new TelasOcultasDTO(
                List.of(Tela.RELATORIOS, Tela.TRANSFERENCIAS, Tela.RELATORIOS));

        var telas = Assertions.assertDoesNotThrow(() -> telaService.atualizarOcultas(telasOcultasDTO));
        Assertions.assertNotNull(telas);
    }
}