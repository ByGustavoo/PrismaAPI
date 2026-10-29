package br.com.prismaapi.controller.conta;

import br.com.prismaapi.model.dto.conta.ContaDTO;
import br.com.prismaapi.model.dto.conta.OrigemDTO;
import br.com.prismaapi.model.dto.conta.SalvarContaDTO;
import br.com.prismaapi.service.conta.ContaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/contas")
public class ContaController implements ContaDocs {

    private final ContaService contaService;

    @Override
    public ResponseEntity<List<ContaDTO>> listarContas() {
        return ResponseEntity.ok(contaService.listar());
    }

    @Override
    public ResponseEntity<List<OrigemDTO>> listarOrigens() {
        return ResponseEntity.ok(contaService.listarOrigens());
    }

    @Override
    public ResponseEntity<ContaDTO> salvarConta(SalvarContaDTO salvarContaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contaService.salvar(salvarContaDTO));
    }

    @Override
    public ResponseEntity<ContaDTO> atualizarConta(UUID id, SalvarContaDTO salvarContaDTO) {
        return ResponseEntity.ok(contaService.atualizar(id, salvarContaDTO));
    }

    @Override
    public ResponseEntity<Void> deletarConta(UUID id) {
        contaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}