package br.com.prismaapi.controller.aviso;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.aviso.AvisoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Tag(name = "Aviso", description = "Endpoints relacionados aos avisos do sino do cabeçalho")
public interface AvisoDocs {

    @Operation(
            summary = "Lista os avisos",
            description = """
                    Retorna os avisos derivados dos dados das outras telas, sem cadastro próprio: faturas \
                    ainda não pagas que vencem nos próximos 15 dias ou venceram há até 15 dias, lançamentos \
                    pendentes ou agendados até 15 dias à frente, inclusive os atrasados, e cartões de crédito \
                    com 70% ou mais do limite comprometido.

                    A severidade é CRITICO para o que vence em até dois dias ou já venceu e para o cartão \
                    a partir de 90% do limite, ATENCAO para o que vence em até sete dias ou para o cartão \
                    entre 70% e 90%, e INFO para o resto; lançamento agendado é sempre INFO. A lista vem \
                    da severidade mais alta para a mais baixa e, dentro de cada uma, pela data.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Avisos retornados com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    ResponseEntity<List<AvisoDTO>> getAvisos();
}