package br.com.prismaapi.service.aviso;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.TipoAviso;
import br.com.prismaapi.model.dto.aviso.PreferenciasAvisosDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class PreferenciasAvisosServiceTest extends AbstractTest {

    @Autowired
    private PreferenciasAvisosService preferenciasAvisosService;

    @Test
    void buscarTest() {
        var preferencias = Assertions.assertDoesNotThrow(() -> preferenciasAvisosService.buscar());
        Assertions.assertNotNull(preferencias);
    }

    @Test
    void atualizarTest() {
        var preferenciasAvisosDTO = new PreferenciasAvisosDTO(
                List.of(TipoAviso.RECEITA_PREVISTA),
                7,
                80,
                60,
                30);

        var preferencias = Assertions.assertDoesNotThrow(() -> preferenciasAvisosService.atualizar(preferenciasAvisosDTO));
        Assertions.assertNotNull(preferencias);
    }
}