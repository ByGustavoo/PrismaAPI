package br.com.prismaapi.controller.aviso;

import br.com.prismaapi.model.dto.aviso.AvisoDTO;
import br.com.prismaapi.service.aviso.AvisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/alerts")
public class AvisoController implements AvisoDocs {

    private final AvisoService avisoService;

    @Override
    public ResponseEntity<List<AvisoDTO>> getAvisos() {
        return ResponseEntity.ok(avisoService.listar());
    }
}