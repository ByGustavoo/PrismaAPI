package br.com.prismaapi.controller.meta;

import br.com.prismaapi.enums.SituacaoMeta;
import br.com.prismaapi.model.dto.meta.AtualizarMetaDTO;
import br.com.prismaapi.model.dto.meta.MetaDTO;
import br.com.prismaapi.model.dto.meta.ResumoMetasDTO;
import br.com.prismaapi.model.dto.meta.SalvarMetaDTO;
import br.com.prismaapi.model.dto.metapreco.SalvarMetaPrecoDTO;
import br.com.prismaapi.service.meta.MetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/goals")
public class MetaController implements MetaDocs {

    private final MetaService metaService;

    @Override
    public ResponseEntity<ResumoMetasDTO> getMetas(SituacaoMeta situacao, String busca) {
        return ResponseEntity.ok(metaService.listar(situacao, busca));
    }

    @Override
    public ResponseEntity<MetaDTO> postMeta(SalvarMetaDTO salvarMetaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.salvar(salvarMetaDTO));
    }

    @Override
    public ResponseEntity<MetaDTO> putMeta(UUID id, AtualizarMetaDTO atualizarMetaDTO) {
        return ResponseEntity.ok(metaService.atualizar(id, atualizarMetaDTO));
    }

    @Override
    public ResponseEntity<MetaDTO> postPreco(UUID id, SalvarMetaPrecoDTO salvarMetaPrecoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.registrarPreco(id, salvarMetaPrecoDTO));
    }

    @Override
    public ResponseEntity<Void> deleteMeta(UUID id) {
        metaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}