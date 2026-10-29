package br.com.prismaapi.controller.aviso;

import br.com.prismaapi.model.dto.aviso.AdiarAvisoDTO;
import br.com.prismaapi.model.dto.aviso.AvisoDTO;
import br.com.prismaapi.model.dto.aviso.PreferenciasAvisosDTO;
import br.com.prismaapi.service.aviso.AvisoService;
import br.com.prismaapi.service.aviso.PreferenciasAvisosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/avisos")
public class AvisoController implements AvisoDocs {

    private final AvisoService avisoService;
    private final PreferenciasAvisosService preferenciasAvisosService;

    @Override
    public ResponseEntity<List<AvisoDTO>> listarAvisos() {
        return ResponseEntity.ok(avisoService.listar());
    }

    @Override
    public ResponseEntity<AvisoDTO> registrarLeituraAviso(String id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avisoService.registrarLeitura(id));
    }

    @Override
    public ResponseEntity<AvisoDTO> registrarAdiamentoAviso(String id, AdiarAvisoDTO adiarAvisoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avisoService.registrarAdiamento(id, adiarAvisoDTO));
    }

    @Override
    public ResponseEntity<Void> deletarLeituraAviso(String id) {
        avisoService.deletarLeitura(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<AvisoDTO>> registrarLeituraAvisos() {
        return ResponseEntity.status(HttpStatus.CREATED).body(avisoService.registrarLeituras());
    }

    @Override
    public ResponseEntity<Void> deletarLeiturasAvisos() {
        avisoService.deletarLeituras();
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PreferenciasAvisosDTO> buscarPreferenciasAvisos() {
        return ResponseEntity.ok(preferenciasAvisosService.buscar());
    }

    @Override
    public ResponseEntity<PreferenciasAvisosDTO> atualizarPreferenciasAvisos(PreferenciasAvisosDTO preferenciasAvisosDTO) {
        return ResponseEntity.ok(preferenciasAvisosService.atualizar(preferenciasAvisosDTO));
    }
}