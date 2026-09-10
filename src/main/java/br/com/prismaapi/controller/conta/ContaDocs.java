package br.com.prismaapi.controller.conta;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.conta.ContaDTO;
import br.com.prismaapi.model.dto.conta.OrigemDTO;
import br.com.prismaapi.model.dto.conta.SalvarContaDTO;
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

import java.util.List;
import java.util.UUID;

@Tag(name = "Conta", description = "Endpoints relacionados às contas")
public interface ContaDocs {

    @Operation(
            summary = "Lista as contas",
            description = """
                    Retorna todas as contas, com as ativas primeiro. Dentro de cada grupo, a ordem é \
                    alfabética pelo nome e, no empate, pela instituição.

                    Conta inativa continua na lista: ela sai do saldo total e dos seletores de \
                    lançamento, mas o histórico dela segue legível.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contas retornadas com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    ResponseEntity<List<ContaDTO>> getContas();

    @Operation(
            summary = "Lista as origens de dinheiro",
            description = """
                    Retorna contas e cartões na mesma lista, do jeito que os seletores de lançamento \
                    precisam: as contas primeiro e os cartões depois, cada grupo em ordem alfabética.

                    Só entram registros ativos. O cartão de débito fica de fora, porque ele é apenas \
                    o meio de acessar a conta, que já está na lista.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Origens retornadas com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/sources")
    ResponseEntity<List<OrigemDTO>> getOrigens();

    @Operation(
            summary = "Cadastra uma conta",
            description = """
                    Cadastra a conta e devolve o registro salvo. Saldo negativo é aceito: conta no \
                    vermelho existe.

                    Nome e instituição são gravados sem os espaços das pontas, e duas contas não podem \
                    ter o mesmo nome na mesma instituição, sem diferenciar maiúsculas. Conta inativa \
                    nunca entra no saldo total, independentemente do incluirNoTotal enviado.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Conta cadastrada com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Já existe uma conta com esse nome nessa instituição!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    ResponseEntity<ContaDTO> postConta(@RequestBody @Valid SalvarContaDTO salvarContaDTO);

    @Operation(
            summary = "Atualiza uma conta",
            description = """
                    Substitui a conta inteira pelo corpo enviado, com as mesmas regras do cadastro.

                    A checagem de duplicidade ignora a própria conta, então salvar sem mudar o nome \
                    não acusa conflito. Inativar a conta tira ela do saldo total.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Conta atualizada com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição ou id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta não encontrada!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Já existe uma conta com esse nome nessa instituição!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PutMapping("/{id}")
    ResponseEntity<ContaDTO> putConta(
            @Parameter(description = "Id da conta")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarContaDTO salvarContaDTO);

    @Operation(
            summary = "Exclui uma conta",
            description = """
                    Remove a conta de forma definitiva e responde sem corpo.

                    Excluir não apaga histórico: se a conta aparecer em qualquer lançamento, como \
                    origem ou como destino de transferência, a exclusão é recusada. Nesse caso, o \
                    caminho é marcar a conta como inativa, o que a tira do saldo total e dos seletores \
                    sem esconder o passado.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Conta excluída com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta não encontrada!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "A conta tem lançamentos no histórico!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteConta(
            @Parameter(description = "Id da conta")
            @PathVariable UUID id);
}