package br.com.prismaapi.controller.cartao;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.cartao.CartaoDTO;
import br.com.prismaapi.model.dto.cartao.SalvarCartaoDTO;
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

@Tag(name = "Cartão", description = "Endpoints relacionados aos cartões")
public interface CartaoDocs {

    @Operation(
            summary = "Lista os cartões",
            description = """
                    Retorna os cartões de crédito primeiro, depois os de débito e os vales. Dentro de \
                    cada tipo, os ativos vêm antes dos inativos, em ordem alfabética pelo nome e, no \
                    empate, pela instituição.

                    Cada cartão traz só os campos do seu tipo: crédito tem limite e datas de fatura, \
                    débito aponta para a conta que acessa e os vales carregam saldo próprio. O limite \
                    comprometido do crédito é calculado a cada leitura, somando as faturas ainda não \
                    pagas, inclusive as futuras formadas por parcelas já assumidas.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cartões retornados com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    ResponseEntity<List<CartaoDTO>> listarCartoes();

    @Operation(
            summary = "Cadastra um cartão",
            description = """
                    Cadastra o cartão e devolve o registro salvo, com o limite comprometido já \
                    calculado quando ele é de crédito.

                    Só os campos do tipo escolhido são gravados: crédito exige limite e dias de \
                    fechamento e vencimento, débito exige a conta vinculada e os vales exigem o saldo. \
                    O que vier de outro tipo é descartado. Nome, instituição, bandeira e últimos \
                    dígitos são gravados sem os espaços das pontas.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cartão cadastrado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Conta vinculada ao cartão de débito inexistente!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    ResponseEntity<CartaoDTO> salvarCartao(@RequestBody @Valid SalvarCartaoDTO salvarCartaoDTO);

    @Operation(
            summary = "Atualiza um cartão",
            description = """
                    Substitui o cartão inteiro pelo corpo enviado, com as mesmas regras do cadastro.

                    Os campos do tipo são refeitos a partir do corpo: o que não vier é limpo, e não \
                    preservado. Trocar um cartão de crédito por vale remove limite e dias de fatura, \
                    e trocar débito por crédito desfaz o vínculo com a conta.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cartão atualizado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição ou id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cartão não encontrado!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Conta vinculada ao cartão de débito inexistente!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PutMapping("/{id}")
    ResponseEntity<CartaoDTO> atualizarCartao(
            @Parameter(description = "Id do cartão")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarCartaoDTO salvarCartaoDTO);

    @Operation(
            summary = "Exclui um cartão",
            description = """
                    Remove o cartão de forma definitiva e responde sem corpo.

                    Excluir não apaga histórico: se o cartão for origem de algum lançamento ou de \
                    alguma despesa recorrente, ou tiver compras parceladas, a exclusão é recusada. \
                    Nesse caso, o caminho é marcar o cartão \
                    como inativo, o que o tira dos novos lançamentos sem esconder o passado.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Cartão excluído com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cartão não encontrado!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "O cartão tem registros no histórico!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deletarCartao(
            @Parameter(description = "Id do cartão")
            @PathVariable UUID id);
}