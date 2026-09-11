package br.com.prismaapi.controller.categoria;

import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Categoria", description = "Endpoints relacionados ao catálogo de categorias")
public interface CategoriaDocs {

    @Operation(
            summary = "Lista as categorias",
            description = """
                    Retorna o catálogo de categorias em ordem alfabética pelo nome.

                    Receita e despesa não compartilham categoria: o filtro por tipo é o que faz \
                    cada formulário oferecer apenas as do seu lado. Sem ele, devolve todas.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorias retornadas com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Tipo de categoria inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    ResponseEntity<List<CategoriaDTO>> listarCategorias(
            @Parameter(description = "Tipo da categoria", example = "DESPESA")
            @RequestParam(required = false) TipoCategoria tipo);
}