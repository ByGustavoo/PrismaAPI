package br.com.prismaapi.controller.orcamento;

import br.com.prismaapi.model.dto.orcamento.OrcamentoDTO;
import br.com.prismaapi.model.dto.orcamento.SalvarOrcamentoDTO;
import br.com.prismaapi.model.dto.orcamento.VisaoGeralOrcamentoDTO;
import br.com.prismaapi.service.orcamento.OrcamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/orcamentos")
public class OrcamentoController implements OrcamentoDocs {

    private final OrcamentoService orcamentoService;

    @Override
    public ResponseEntity<VisaoGeralOrcamentoDTO> buscarVisaoGeral(YearMonth mes) {
        return ResponseEntity.ok(orcamentoService.visaoGeral(mes));
    }

    @Override
    public ResponseEntity<OrcamentoDTO> salvarOrcamento(SalvarOrcamentoDTO salvarOrcamentoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orcamentoService.salvar(salvarOrcamentoDTO));
    }

    @Override
    public ResponseEntity<OrcamentoDTO> atualizarOrcamento(UUID id, SalvarOrcamentoDTO salvarOrcamentoDTO) {
        return ResponseEntity.ok(orcamentoService.atualizar(id, salvarOrcamentoDTO));
    }

    @Override
    public ResponseEntity<Void> deletarOrcamento(UUID id) {
        orcamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}