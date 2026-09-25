package br.com.prismaapi.service.conta;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.repository.conta.ContaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EvolucaoContaServiceTest extends AbstractTest {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private EvolucaoContaService evolucaoContaService;

    @Test
    void listarReservasTest() {
        var reservas = Assertions.assertDoesNotThrow(() -> evolucaoContaService.listarReservas());
        Assertions.assertNotNull(reservas);
    }

    @Test
    void buscarEvolucaoTest() {
        var idConta = buscar(contaRepository, conta -> conta.getNome().equals("Reserva de emergência")).getId();

        var evolucao = Assertions.assertDoesNotThrow(() -> evolucaoContaService.buscarEvolucao(idConta));
        Assertions.assertNotNull(evolucao);
    }
}