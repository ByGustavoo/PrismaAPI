package br.com.prismaapi.controller.sistema;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.sistema.VersaoSistemaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Sistema", description = "Endpoints relacionados às informações da própria API")
public interface SistemaDocs {

    @Operation(
            summary = "Busca a versão da API",
            description = """
                    Retorna a versão declarada no build e o instante em UTC em que o artefato foi gerado.

                    Não acessa o banco: é a chamada que diz se a API está no ar.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Versão retornada com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/versao")
    ResponseEntity<VersaoSistemaDTO> buscarVersao();
}