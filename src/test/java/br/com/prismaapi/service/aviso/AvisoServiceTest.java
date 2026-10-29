package br.com.prismaapi.service.aviso;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.model.dto.aviso.AdiarAvisoDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AvisoServiceTest extends AbstractTest {

    @Autowired
    private AvisoService avisoService;

    @Test
    void listarTest() {
        var avisos = Assertions.assertDoesNotThrow(() -> avisoService.listar());
        Assertions.assertNotNull(avisos);
    }

    @Test
    void registrarLeituraTest() {
        var idAviso = avisoService.listar().getFirst().id();

        var aviso = Assertions.assertDoesNotThrow(() -> avisoService.registrarLeitura(idAviso));
        Assertions.assertNotNull(aviso);
    }

    @Test
    void registrarAdiamentoTest() {
        var idAviso = avisoService.listar().getFirst().id();
        var adiarAvisoDTO = new AdiarAvisoDTO(
                3);

        var aviso = Assertions.assertDoesNotThrow(() -> avisoService.registrarAdiamento(idAviso, adiarAvisoDTO));
        Assertions.assertNotNull(aviso);
    }

    @Test
    void deletarLeituraTest() {
        var idAviso = avisoService.listar().getFirst().id();

        avisoService.registrarLeitura(idAviso);

        Assertions.assertDoesNotThrow(() -> avisoService.deletarLeitura(idAviso));
    }

    @Test
    void registrarLeiturasTest() {
        var avisos = Assertions.assertDoesNotThrow(() -> avisoService.registrarLeituras());
        Assertions.assertNotNull(avisos);
    }

    @Test
    void deletarLeiturasTest() {
        Assertions.assertDoesNotThrow(() -> avisoService.deletarLeituras());
    }
}