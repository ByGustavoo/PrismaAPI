package br.com.prismaapi.controller.compraparcelada;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.compraparcelada.CompraParceladaDTO;
import br.com.prismaapi.model.dto.compraparcelada.PlanoCompraParceladaDTO;
import br.com.prismaapi.model.dto.compraparcelada.SalvarCompraParceladaDTO;
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

@Tag(name = "Compra Parcelada", description = "Endpoints relacionados às compras parceladas no cartão de crédito")
public interface CompraParceladaDocs {

    @Operation(
            summary = "Lista as compras parceladas",
            description = """
                    Retorna cada compra com o cronograma de parcelas e os totais já calculados: as compras \
                    em andamento primeiro, da mais recente para a mais antiga, e as quitadas ao fim.

                    Cada parcela cai na fatura do seu mês e vence junto com ela. As primeiras levam o \
                    valor arredondado para baixo e a última absorve a sobra, para a soma fechar com o \
                    valor total. A parcela vencida é PAGA, a primeira ainda não vencida é a ATUAL e as \
                    demais são FUTURA; parcelaAtual vem nula quando a compra já foi quitada.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Compras parceladas retornadas com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Filtro inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    ResponseEntity<List<PlanoCompraParceladaDTO>> getComprasParceladas(
            @Parameter(description = "Id do cartão; sem ele, retorna as compras de todos os cartões")
            @RequestParam(required = false) UUID idCartao);

    @Operation(
            summary = "Cadastra uma compra parcelada",
            description = """
                    Cadastra a compra e devolve o registro salvo, sem o cronograma calculado. As parcelas \
                    não viram lançamentos: elas entram nas faturas, no limite comprometido do cartão e \
                    no cronograma pelo cálculo, a partir do primeiro mês informado.

                    Só cartões de crédito aceitam compras parceladas. A categoria é opcional, mas, se \
                    vier, precisa existir. Descrição e observações são gravadas sem os espaços das pontas.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Compra parcelada cadastrada com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Cartão ou categoria recusados pela regra de negócio!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    ResponseEntity<CompraParceladaDTO> postCompraParcelada(@RequestBody @Valid SalvarCompraParceladaDTO salvarCompraParceladaDTO);

    @Operation(
            summary = "Atualiza uma compra parcelada",
            description = """
                    Substitui a compra inteira pelo corpo enviado, com as mesmas regras do cadastro, e \
                    devolve o registro salvo sem o cronograma calculado.

                    Categoria e observações que não vierem no corpo são limpas, e não preservadas. Como \
                    as parcelas saem do cálculo, mudar o valor, a quantidade ou o primeiro mês refaz as \
                    faturas e o cronograma na próxima leitura.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Compra parcelada atualizada com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição ou id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compra parcelada não encontrada!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Cartão ou categoria recusados pela regra de negócio!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PutMapping("/{id}")
    ResponseEntity<CompraParceladaDTO> putCompraParcelada(
            @Parameter(description = "Id da compra parcelada")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarCompraParceladaDTO salvarCompraParceladaDTO);

    @Operation(
            summary = "Exclui uma compra parcelada",
            description = """
                    Remove a compra de forma definitiva e responde sem corpo.

                    Como as parcelas não são lançamentos, excluir a compra tira todas elas das faturas, \
                    do limite comprometido do cartão e do cronograma, inclusive as que já venceram.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Compra parcelada excluída com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compra parcelada não encontrada!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteCompraParcelada(
            @Parameter(description = "Id da compra parcelada")
            @PathVariable UUID id);
}