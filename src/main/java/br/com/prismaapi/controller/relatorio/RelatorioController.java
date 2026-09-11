package br.com.prismaapi.controller.relatorio;

import br.com.prismaapi.model.dto.relatorio.RelatorioDTO;
import br.com.prismaapi.service.relatorio.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/relatorios")
public class RelatorioController implements RelatorioDocs {

    private final RelatorioService relatorioService;

    @Override
    public ResponseEntity<RelatorioDTO> buscarResumo(LocalDate dataInicial, LocalDate dataFinal) {
        return ResponseEntity.ok(relatorioService.resumir(dataInicial, dataFinal));
    }
}