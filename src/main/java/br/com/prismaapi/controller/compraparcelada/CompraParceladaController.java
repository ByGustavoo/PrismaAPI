package br.com.prismaapi.controller.compraparcelada;

import br.com.prismaapi.model.dto.compraparcelada.CompraParceladaDTO;
import br.com.prismaapi.model.dto.compraparcelada.PlanoCompraParceladaDTO;
import br.com.prismaapi.model.dto.compraparcelada.SalvarCompraParceladaDTO;
import br.com.prismaapi.service.compraparcelada.CompraParceladaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/compras-parceladas")
public class CompraParceladaController implements CompraParceladaDocs {

    private final CompraParceladaService compraParceladaService;

    @Override
    public ResponseEntity<List<PlanoCompraParceladaDTO>> listarComprasParceladas(UUID idCartao) {
        return ResponseEntity.ok(compraParceladaService.listar(idCartao));
    }

    @Override
    public ResponseEntity<CompraParceladaDTO> salvarCompraParcelada(SalvarCompraParceladaDTO salvarCompraParceladaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraParceladaService.salvar(salvarCompraParceladaDTO));
    }

    @Override
    public ResponseEntity<CompraParceladaDTO> atualizarCompraParcelada(UUID id, SalvarCompraParceladaDTO salvarCompraParceladaDTO) {
        return ResponseEntity.ok(compraParceladaService.atualizar(id, salvarCompraParceladaDTO));
    }

    @Override
    public ResponseEntity<Void> deletarCompraParcelada(UUID id) {
        compraParceladaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}