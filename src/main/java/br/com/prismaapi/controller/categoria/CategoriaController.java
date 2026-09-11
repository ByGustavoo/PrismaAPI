package br.com.prismaapi.controller.categoria;

import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import br.com.prismaapi.service.categoria.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/categorias")
public class CategoriaController implements CategoriaDocs {

    private final CategoriaService categoriaService;

    @Override
    public ResponseEntity<List<CategoriaDTO>> listarCategorias(TipoCategoria tipo) {
        return ResponseEntity.ok(categoriaService.listar(tipo));
    }
}