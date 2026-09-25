package br.com.prismaapi.service.dashboard;

import br.com.prismaapi.config.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.YearMonth;

@SpringBootTest
class DashboardServiceTest extends AbstractTest {

    @Autowired
    private DashboardService dashboardService;

    @Test
    void resumirTest() {
        var dashboard = Assertions.assertDoesNotThrow(() -> dashboardService.resumir(YearMonth.now().minusMonths(5), YearMonth.now()));
        Assertions.assertNotNull(dashboard);
    }
}