package br.com.prismaapi.controller.lancamento;

import br.com.prismaapi.enums.DirecaoOrdenacao;
import br.com.prismaapi.enums.OrdenacaoLancamento;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.dto.lancamento.FiltroLancamentoDTO;
import br.com.prismaapi.model.dto.lancamento.LancamentoDTO;
import br.com.prismaapi.model.dto.lancamento.PaginaLancamentosDTO;
import br.com.prismaapi.model.dto.lancamento.SalvarLancamentoDTO;
import br.com.prismaapi.service.lancamento.LancamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/lancamentos")
public class LancamentoController implements LancamentoDocs {

    private final LancamentoService lancamentoService;

    @Override
    public ResponseEntity<PaginaLancamentosDTO> listarLancamentos(TipoLancamento tipo, String busca, LocalDate dataInicial, LocalDate dataFinal, UUID idCategoria, UUID idOrigem, SituacaoLancamento situacao, int pagina, int tamanho, OrdenacaoLancamento ordenacao, DirecaoOrdenacao direcao) {
        var filtro = new FiltroLancamentoDTO(tipo, busca, dataInicial, dataFinal, idCategoria, idOrigem, situacao, pagina, tamanho, ordenacao, direcao);
        return ResponseEntity.ok(lancamentoService.listar(filtro));
    }

    @Override
    public ResponseEntity<LancamentoDTO> buscarLancamento(UUID id) {
        return ResponseEntity.ok(lancamentoService.buscarPorId(id));
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