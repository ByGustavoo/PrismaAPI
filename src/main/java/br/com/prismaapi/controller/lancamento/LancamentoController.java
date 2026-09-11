package br.com.prismaapi.controller.lancamento;

import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.dto.lancamento.FiltroLancamentoDTO;
import br.com.prismaapi.model.dto.lancamento.LancamentoDTO;
import br.com.prismaapi.model.dto.lancamento.SalvarLancamentoDTO;
import br.com.prismaapi.service.lancamento.LancamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/lancamentos")
public class LancamentoController implements LancamentoDocs {

    private final LancamentoService lancamentoService;

    @Override
    public ResponseEntity<List<LancamentoDTO>> listarLancamentos(TipoLancamento tipo, String busca, LocalDate dataInicial, LocalDate dataFinal, UUID idCategoria, UUID idOrigem, SituacaoLancamento situacao) {
        var filtro = new FiltroLancamentoDTO(tipo, busca, dataInicial, dataFinal, idCategoria, idOrigem, situacao);
        return ResponseEntity.ok(lancamentoService.listar(filtro));
    }

    @Override
    public ResponseEntity<LancamentoDTO> salvarLancamento(SalvarLancamentoDTO salvarLancamentoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lancamentoService.salvar(salvarLancamentoDTO));
    }

    @Override
    public ResponseEntity<LancamentoDTO> atualizarLancamento(UUID id, SalvarLancamentoDTO salvarLancamentoDTO) {
        return ResponseEntity.ok(lancamentoService.atualizar(id, salvarLancamentoDTO));
    }

    @Override
    public ResponseEntity<Void> deletarLancamento(UUID id) {
        lancamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}