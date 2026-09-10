package br.com.prismaapi.controller.despesarecorrente;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.despesarecorrente.DespesaRecorrenteDTO;
import br.com.prismaapi.model.dto.despesarecorrente.ResumoDespesasRecorrentesDTO;
import br.com.prismaapi.model.dto.despesarecorrente.SalvarDespesaRecorrenteDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Despesa Recorrente", description = "Endpoints relacionados às despesas fixas que se repetem")
public interface DespesaRecorrenteDocs {

    @Operation(
            summary = "Lista as despesas recorrentes",
            description = """
                    Retorna as despesas já consolidadas: a lista completa, o custo mensal e anual \
                    equivalentes e as que vencem nos próximos sete dias. A lista vem pelo próximo \
                    vencimento, com as pausadas ao fim; os vencimentos próximos seguem a mesma ordem.

                    O custo mensal normaliza cada frequência para o mês: a semanal é multiplicada por \
                    4,3452, a média real de semanas num mês, a quinzenal por 2,1726, e as demais são \
                    divididas pelo número de meses entre uma ocorrência e outra. O custo anual é o \
                    mensal vezes doze. Os dois contam só as despesas ativas.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Despesas recorrentes retornadas com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    ResponseEntity<ResumoDespesasRecorrentesDTO> getDespesasRecorrentes();

    @Operation(
            summary = "Cadastra uma despesa recorrente",
            description = """
                    Cadastra a despesa e devolve o registro salvo, com a origem e a categoria resolvidas \
                    pelo servidor. A origem pode ser uma conta ou um cartão.

                    A categoria é opcional, mas, se vier, precisa ser de despesa. Descrição e observações \
                    são gravadas sem os espaços das pontas.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Despesa recorrente cadastrada com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Origem ou categoria recusadas pela regra de negócio!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    ResponseEntity<DespesaRecorrenteDTO> postDespesaRecorrente(@RequestBody @Valid SalvarDespesaRecorrenteDTO salvarDespesaRecorrenteDTO);

    @Operation(
            summary = "Atualiza uma despesa recorrente",
            description = """
                    Substitui a despesa inteira pelo corpo enviado, com as mesmas regras do cadastro.

                    Pausar e retomar é este mesmo PUT com a situação trocada: a despesa pausada continua \
                    no cadastro e sai do custo mensal e dos vencimentos próximos. Categoria e observações \
                    que não vierem no corpo são limpas, e não preservadas.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Despesa recorrente atualizada com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição ou id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Despesa recorrente não encontrada!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Origem ou categoria recusadas pela regra de negócio!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PutMapping("/{id}")
    ResponseEntity<DespesaRecorrenteDTO> putDespesaRecorrente(
            @Parameter(description = "Id da despesa recorrente")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarDespesaRecorrenteDTO salvarDespesaRecorrenteDTO);

    @Operation(
            summary = "Exclui uma despesa recorrente",
            description = """
                    Remove a despesa de forma definitiva e responde sem corpo.

                    Os lançamentos já feitos continuam no histórico; o que se perde são as próximas \
                    ocorrências na previsão. Para só suspender a despesa, pause-a.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Despesa recorrente excluída com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Despesa recorrente não encontrada!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteDespesaRecorrente(
            @Parameter(description = "Id da despesa recorrente")
            @PathVariable UUID id);
}