package br.com.prismaapi.service.recorrente;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.repository.recorrente.RecorrenteRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
class LancamentoRecorrenteServiceTest extends AbstractTest {

    @Autowired
    private RecorrenteRepository recorrenteRepository;

    @Autowired
    private LancamentoRecorrenteService lancamentoRecorrenteService;

    @Test
    void lancarVencidasTest() {
        var lancadas = Assertions.assertDoesNotThrow(() -> lancamentoRecorrenteService.lancarVencidas());
        Assertions.assertNotNull(lancadas);
    }

    @Test
    void lancarTest() {
        var despesa = buscar(recorrenteRepository, recorrente -> recorrente.getDescricao().equals("Streaming"));

        var lancadas = Assertions.assertDoesNotThrow(() -> lancamentoRecorrenteService.lancar(despesa, LocalDate.now().plusMonths(1)));
        Assertions.assertNotNull(lancadas);
    }

    @Test
    void lancaSozinhaTest() {
        var despesa = buscar(recorrenteRepository, recorrente -> recorrente.getDescricao().equals("Streaming"));

        var lancaSozinha = Assertions.assertDoesNotThrow(() -> LancamentoRecorrenteService.lancaSozinha(despesa));
        Assertions.assertNotNull(lancaSozinha);
    }
}