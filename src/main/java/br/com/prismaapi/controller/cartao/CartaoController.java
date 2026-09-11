package br.com.prismaapi.controller.cartao;

import br.com.prismaapi.model.dto.cartao.CartaoDTO;
import br.com.prismaapi.model.dto.cartao.SalvarCartaoDTO;
import br.com.prismaapi.service.cartao.CartaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/cartoes")
public class CartaoController implements CartaoDocs {

    private final CartaoService cartaoService;

    @Override
    public ResponseEntity<List<CartaoDTO>> listarCartoes() {
        return ResponseEntity.ok(cartaoService.listar());
    }

    @Override
    public ResponseEntity<CartaoDTO> salvarCartao(SalvarCartaoDTO salvarCartaoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartaoService.salvar(salvarCartaoDTO));
    }

    @Override
    public ResponseEntity<CartaoDTO> atualizarCartao(UUID id, SalvarCartaoDTO salvarCartaoDTO) {
        return ResponseEntity.ok(cartaoService.atualizar(id, salvarCartaoDTO));
    }

    @Override
    public ResponseEntity<Void> deletarCartao(UUID id) {
        cartaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}