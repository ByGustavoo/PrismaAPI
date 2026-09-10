package br.com.prismaapi.controller.orcamento;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.orcamento.OrcamentoDTO;
import br.com.prismaapi.model.dto.orcamento.SalvarOrcamentoDTO;
import br.com.prismaapi.model.dto.orcamento.VisaoGeralOrcamentoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.UUID;

@Tag(name = "Orçamento", description = "Endpoints relacionados aos limites mensais por categoria de despesa")
public interface OrcamentoDocs {

    @Operation(
            summary = "Consumo dos orçamentos no mês",
            description = """
                    Retorna, para o mês consultado, cada limite com o gasto, o que resta, o consumo e a \
                    situação, além dos totais planejados e das categorias que tiveram gasto sem limite \
                    definido. Não existe listagem crua: os limites são lidos daqui.

                    O limite não tem mês e vale até ser alterado; o gasto é apurado pelas despesas do mês. \
                    O consumo é fração e passa de 1 no estouro, e o restante fica negativo. A situação é \
                    SEGURO abaixo de 80% do limite, ALERTA a partir de 80% e ESTOURADO a partir de 100%. \
                    A projeção só é calculada a partir do décimo dia do mês e vem zerada antes disso. Os \
                    itens vêm do maior consumo para o menor.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Consumo dos orçamentos retornado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Mês inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/overview")
    ResponseEntity<VisaoGeralOrcamentoDTO> getVisaoGeral(
            @Parameter(description = "Mês consultado; sem ele, o mês corrente", example = "2026-09")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes);

    @Operation(
            summary = "Cadastra um orçamento",
            description = """
                    Define o limite mensal de uma categoria e devolve o registro salvo, com a categoria \
                    resolvida.

                    Só categorias de despesa aceitam orçamento, e cada categoria tem no máximo um limite: \
                    para mudar o valor, edite o orçamento existente.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Orçamento cadastrado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "A categoria já tem orçamento!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Categoria inexistente ou de receita!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    ResponseEntity<OrcamentoDTO> postOrcamento(@RequestBody @Valid SalvarOrcamentoDTO salvarOrcamentoDTO);

    @Operation(
            summary = "Atualiza um orçamento",
            description = """
                    Substitui a categoria e o limite pelo corpo enviado, com as mesmas regras do cadastro.

                    Trocar a categoria é permitido desde que a nova ainda não tenha limite. O novo valor \
                    passa a valer para todos os meses, inclusive os já encerrados, porque o orçamento não \
                    guarda histórico de limites.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orçamento atualizado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição ou id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Orçamento não encontrado!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "A categoria já tem orçamento!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Categoria inexistente ou de receita!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PutMapping("/{id}")
    ResponseEntity<OrcamentoDTO> putOrcamento(
            @Parameter(description = "Id do orçamento")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarOrcamentoDTO salvarOrcamentoDTO);

    @Operation(
            summary = "Exclui um orçamento",
            description = """
                    Remove o limite de forma definitiva e responde sem corpo.

                    As despesas da categoria continuam onde estão: sem o limite, ela passa a aparecer \
                    entre as categorias com gasto fora do orçamento.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Orçamento excluído com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Orçamento não encontrado!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteOrcamento(
            @Parameter(description = "Id do orçamento")
            @PathVariable UUID id);
}