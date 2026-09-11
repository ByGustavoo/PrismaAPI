package br.com.prismaapi.controller.fatura;

import br.com.prismaapi.model.dto.fatura.DetalheFaturaDTO;
import br.com.prismaapi.model.dto.fatura.FaturaCartaoDTO;
import br.com.prismaapi.service.fatura.FaturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/faturas")
public class FaturaController implements FaturaDocs {

    private final FaturaService faturaService;

    @Override
    public ResponseEntity<List<FaturaCartaoDTO>> listarFaturas(UUID idCartao) {
        return ResponseEntity.ok(faturaService.listar(idCartao));
    }

    @Override
    public ResponseEntity<DetalheFaturaDTO> buscarFatura(String id) {
        return ResponseEntity.ok(faturaService.detalhar(id));
    }
}