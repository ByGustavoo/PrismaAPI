package br.com.prismaapi.service.dashboard;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.model.dto.dashboard.variacao.VariacaoDTO;
import br.com.prismaapi.service.investimento.InvestimentoService;
import br.com.prismaapi.util.ValorUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.YearMonth;
import java.util.List;

@SpringBootTest
class DashboardServiceTest extends AbstractTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private InvestimentoService investimentoService;

    @Test
    void resumirTest() {
        var dashboard = Assertions.assertDoesNotThrow(() -> dashboardService.resumir(YearMonth.now().minusMonths(5), YearMonth.now()));
        Assertions.assertNotNull(dashboard);
    }

    @Test
    void resumirInvestimentosNoCorteTest() {
        var mes = YearMonth.now().minusMonths(6);
        var carteira = investimentoService.evolucaoNosMeses(List.of(mes)).getFirst();

        var dashboard = Assertions.assertDoesNotThrow(() -> dashboardService.resumir(mes, mes));
        Assertions.assertEquals(ValorUtil.dinheiro(carteira.valor()), dashboard.totalInvestido());
        Assertions.assertEquals(VariacaoDTO.entre(carteira.valor(), carteira.aportado()), dashboard.variacaoInvestimentos());
    }
}