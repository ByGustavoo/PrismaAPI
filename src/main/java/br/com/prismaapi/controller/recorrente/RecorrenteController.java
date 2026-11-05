package br.com.prismaapi.controller.recorrente;

import br.com.prismaapi.model.dto.recorrente.RecorrenteDTO;
import br.com.prismaapi.model.dto.recorrente.ResumoRecorrentesDTO;
import br.com.prismaapi.model.dto.recorrente.SalvarRecorrenteDTO;
import br.com.prismaapi.service.recorrente.RecorrenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/recorrentes")
public class RecorrenteController implements RecorrenteDocs {

    private final RecorrenteService recorrenteService;

    @Override
    public ResponseEntity<ResumoRecorrentesDTO> listarRecorrentes() {
        return ResponseEntity.ok(recorrenteService.resumir());
    }

    @Override
    public ResponseEntity<RecorrenteDTO> salvarRecorrente(SalvarRecorrenteDTO salvarRecorrenteDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recorrenteService.salvar(salvarRecorrenteDTO));
    }

    @Override
    public ResponseEntity<RecorrenteDTO> atualizarRecorrente(UUID id, SalvarRecorrenteDTO salvarRecorrenteDTO) {
        return ResponseEntity.ok(recorrenteService.atualizar(id, salvarRecorrenteDTO));
    }

    @Override
    public ResponseEntity<Void> deletarRecorrente(UUID id) {
        recorrenteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}