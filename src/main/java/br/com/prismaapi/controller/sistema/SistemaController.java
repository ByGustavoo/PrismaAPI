package br.com.prismaapi.controller.sistema;

import br.com.prismaapi.model.dto.sistema.VersaoSistemaDTO;
import br.com.prismaapi.service.sistema.SistemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/sistema")
public class SistemaController implements SistemaDocs {

    private final SistemaService sistemaService;

    @Override
    public ResponseEntity<VersaoSistemaDTO> buscarVersao() {
        return ResponseEntity.ok(sistemaService.buscarVersao());
    }
}