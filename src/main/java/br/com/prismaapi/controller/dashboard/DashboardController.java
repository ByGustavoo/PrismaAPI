package br.com.prismaapi.controller.dashboard;

import br.com.prismaapi.model.dto.dashboard.DashboardDTO;
import br.com.prismaapi.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/dashboard")
public class DashboardController implements DashboardDocs {

    private final DashboardService dashboardService;

    @Override
    public ResponseEntity<DashboardDTO> getResumo(YearMonth dataInicial, YearMonth dataFinal) {
        return ResponseEntity.ok(dashboardService.resumir(dataInicial, dataFinal));
    }
}