package br.com.prismaapi.controller.tela;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.tela.TelasOcultasDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Tela", description = "Endpoints relacionados às telas que aparecem no menu lateral")
public interface TelaDocs {

    @GetMapping("/ocultas")
    @Operation(
            summary = "Busca as telas ocultas",
            description = """
                    Retorna as telas que o usuário escolheu ocultar em Configurações, na ordem do menu \
                    lateral. Dashboard e Configurações nunca ficam ocultos e por isso não estão entre as \
                    opções. Ocultar uma tela não apaga dado nenhum: só a tira do menu e dos atalhos.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Telas ocultas retornadas com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<TelasOcultasDTO> buscarTelasOcultas();

    @PutMapping("/ocultas")
    @Operation(
            summary = "Atualiza as telas ocultas",
            description = """
                    Substitui a lista inteira: a tela que não vier na requisição volta a aparecer, e lista \
                    vazia mostra todas. Tela repetida conta uma vez, e valor fora das opções responde 400.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Telas ocultas atualizadas com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<TelasOcultasDTO> atualizarTelasOcultas(@RequestBody @Valid TelasOcultasDTO telasOcultasDTO);
}