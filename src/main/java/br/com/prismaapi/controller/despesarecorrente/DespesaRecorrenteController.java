package br.com.prismaapi.controller.despesarecorrente;

import br.com.prismaapi.model.dto.despesarecorrente.DespesaRecorrenteDTO;
import br.com.prismaapi.model.dto.despesarecorrente.ResumoDespesasRecorrentesDTO;
import br.com.prismaapi.model.dto.despesarecorrente.SalvarDespesaRecorrenteDTO;
import br.com.prismaapi.service.despesarecorrente.DespesaRecorrenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/recurring-expenses")
public class DespesaRecorrenteController implements DespesaRecorrenteDocs {

    private final DespesaRecorrenteService despesaRecorrenteService;

    @Override
    public ResponseEntity<ResumoDespesasRecorrentesDTO> getDespesasRecorrentes() {
        return ResponseEntity.ok(despesaRecorrenteService.resumir());
    }

    @Override
    public ResponseEntity<DespesaRecorrenteDTO> postDespesaRecorrente(SalvarDespesaRecorrenteDTO salvarDespesaRecorrenteDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(despesaRecorrenteService.salvar(salvarDespesaRecorrenteDTO));
    }

    @Override
    public ResponseEntity<DespesaRecorrenteDTO> putDespesaRecorrente(UUID id, SalvarDespesaRecorrenteDTO salvarDespesaRecorrenteDTO) {
        return ResponseEntity.ok(despesaRecorrenteService.atualizar(id, salvarDespesaRecorrenteDTO));
    }

    @Override
    public ResponseEntity<Void> deleteDespesaRecorrente(UUID id) {
        despesaRecorrenteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}