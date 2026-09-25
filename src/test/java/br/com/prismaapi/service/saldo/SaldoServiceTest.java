package br.com.prismaapi.service.saldo;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
class SaldoServiceTest extends AbstractTest {

    @Autowired
    private SaldoService saldoService;

    @Test
    void linhaDoSaldoTest() {
        var linhaDoSaldo = Assertions.assertDoesNotThrow(() -> saldoService.linhaDoSaldo(List.of(LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(1)), LocalDate.now()));
        Assertions.assertNotNull(linhaDoSaldo);
    }
}