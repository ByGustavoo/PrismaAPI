package br.com.prismaapi.controller.lancamento;

import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.lancamento.LancamentoDTO;
import br.com.prismaapi.model.dto.lancamento.SalvarLancamentoDTO;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Lançamento", description = "Endpoints relacionados a receitas, despesas e transferências")
public interface LancamentoDocs {

    @Operation(
            summary = "Lista os lançamentos",
            description = """
                    Retorna os lançamentos do mais recente para o mais antigo. Receitas, despesas e \
                    transferências usam esta mesma rota, mudando apenas o tipo.

                    Todos os filtros são opcionais e se combinam com E lógico. A busca casa com a \
                    descrição, o nome da categoria e o nome da conta de origem ou de destino, sem \
                    diferenciar maiúscula nem acento.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lançamentos retornados com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Filtros inválidos!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    ResponseEntity<List<LancamentoDTO>> getLancamentos(
            @Parameter(description = "Tipo do lançamento", example = "DESPESA")
            @RequestParam(required = false) TipoLancamento tipo,

            @Parameter(description = "Texto buscado na descrição, na categoria, na origem ou no destino", example = "saude")
            @RequestParam(required = false) String busca,

            @Parameter(description = "Início do período, inclusivo", example = "2026-09-01")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicial,

            @Parameter(description = "Fim do período, inclusivo", example = "2026-09-30")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFinal,

            @Parameter(description = "Id da categoria")
            @RequestParam(required = false) UUID idCategoria,

            @Parameter(description = "Id da conta ou do cartão de origem; em transferências, casa também com a conta de destino")
            @RequestParam(required = false) UUID idOrigem,

            @Parameter(description = "Situação do lançamento", example = "PAGO")
            @RequestParam(required = false) SituacaoLancamento situacao);

    @Operation(
            summary = "Cadastra um lançamento",
            description = """
                    Recebe os ids de origem, destino e categoria e devolve o lançamento com os nomes \
                    já resolvidos pelo servidor. A origem pode ser uma conta ou um cartão.

                    Transferência não tem categoria: o idCategoria é ignorado, a origem precisa ser \
                    uma conta e a conta de destino é obrigatória e diferente da origem.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Lançamento cadastrado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Origem, destino ou categoria recusados pela regra de negócio!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    ResponseEntity<LancamentoDTO> postLancamento(@RequestBody @Valid SalvarLancamentoDTO salvarLancamentoDTO);

    @Operation(
            summary = "Atualiza um lançamento",
            description = """
                    Substitui o lançamento inteiro pelo corpo enviado, com as mesmas regras do cadastro.

                    Origem, destino e categoria são refeitos a partir dos ids: o que não vier no corpo \
                    é limpo, e não preservado. Trocar uma despesa por transferência remove a categoria.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lançamento atualizado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição ou id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Lançamento não encontrado!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Origem, destino ou categoria recusados pela regra de negócio!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PutMapping("/{id}")
    ResponseEntity<LancamentoDTO> putLancamento(
            @Parameter(description = "Id do lançamento")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarLancamentoDTO salvarLancamentoDTO);

    @Operation(
            summary = "Exclui um lançamento",
            description = "Remove o lançamento de forma definitiva e responde sem corpo.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Lançamento excluído com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Lançamento não encontrado!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteLancamento(
            @Parameter(description = "Id do lançamento")
            @PathVariable UUID id);
}