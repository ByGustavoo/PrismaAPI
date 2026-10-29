package br.com.prismaapi.controller.tela;

import br.com.prismaapi.model.dto.tela.TelasOcultasDTO;
import br.com.prismaapi.service.tela.TelaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/telas")
public class TelaController implements TelaDocs {

    private final TelaService telaService;

    @Override
    public ResponseEntity<TelasOcultasDTO> buscarTelasOcultas() {
        return ResponseEntity.ok(telaService.buscarOcultas());
    }

    @Override
    public ResponseEntity<TelasOcultasDTO> atualizarTelasOcultas(TelasOcultasDTO telasOcultasDTO) {
        return ResponseEntity.ok(telaService.atualizarOcultas(telasOcultasDTO));
    }
}