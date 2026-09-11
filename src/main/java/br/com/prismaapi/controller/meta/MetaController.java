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
@RequestMapping("/v1/metas")
public class MetaController implements MetaDocs {

    private final MetaService metaService;

    @Override
    public ResponseEntity<ResumoMetasDTO> listarMetas(SituacaoMeta situacao, String busca) {
        return ResponseEntity.ok(metaService.listar(situacao, busca));
    }

    @Override
    public ResponseEntity<MetaDTO> salvarMeta(SalvarMetaDTO salvarMetaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.salvar(salvarMetaDTO));
    }

    @Override
    public ResponseEntity<MetaDTO> atualizarMeta(UUID id, AtualizarMetaDTO atualizarMetaDTO) {
        return ResponseEntity.ok(metaService.atualizar(id, atualizarMetaDTO));
    }

    @Override
    public ResponseEntity<MetaDTO> registrarPreco(UUID id, SalvarMetaPrecoDTO salvarMetaPrecoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.registrarPreco(id, salvarMetaPrecoDTO));
    }

    @Override
    public ResponseEntity<Void> deletarMeta(UUID id) {
        metaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}