package br.com.prismaapi.controller.previsao;

import br.com.prismaapi.model.dto.previsao.PrevisaoDTO;
import br.com.prismaapi.service.previsao.PrevisaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/previsao")
public class PrevisaoController implements PrevisaoDocs {

    private final PrevisaoService previsaoService;

    @Override
    public ResponseEntity<PrevisaoDTO> buscarPrevisao(Integer meses) {
        return ResponseEntity.ok(previsaoService.prever(meses));
    }
}