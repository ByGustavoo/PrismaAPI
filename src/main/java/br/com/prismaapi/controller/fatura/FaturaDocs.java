package br.com.prismaapi.controller.fatura;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.fatura.DetalheFaturaDTO;
import br.com.prismaapi.model.dto.fatura.FaturaCartaoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Fatura", description = "Endpoints relacionados às faturas dos cartões de crédito")
public interface FaturaDocs {

    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Faturas retornadas com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Filtro inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @Operation(
            summary = "Lista as faturas",
            description = """
                    Retorna as faturas calculadas dos cartões de crédito, da que vence primeiro para a \
                    que vence por último e, no empate, pelo nome do cartão. Fatura não é cadastro: ela \
                    reúne as despesas lançadas no cartão durante o ciclo e as parcelas das compras \
                    parceladas que caem naquele mês.

                    O ciclo vai do fechamento do mês anterior, exclusivo, até o fechamento do mês, \
                    inclusivo, e um mês sem compras nem parcelas não gera fatura. O id da fatura é o id \
                    do cartão seguido do mês, e o totalAnterior traz o total da fatura anterior do mesmo \
                    cartão, quando ela existe.

                    O valorPago soma as parcelas da fatura com pagamento antecipado registrado, e o \
                    valorRestante é o total menos ele. A fatura já fechada sem nada a pagar sai como PAGA.""")
    ResponseEntity<List<FaturaCartaoDTO>> listarFaturas(
            @Parameter(description = "Id do cartão de crédito; sem ele, retorna as faturas de todos")
            @RequestParam(required = false) UUID idCartao);

    @GetMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fatura retornada com sucesso!"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Fatura não encontrada!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @Operation(
            summary = "Detalha uma fatura",
            description = """
                    Retorna a fatura com os mesmos campos da listagem e os itens que a compõem, do mais \
                    recente para o mais antigo e, no empate, pela descrição.

                    Cada item é uma despesa lançada no cartão durante o ciclo ou a parcela de uma compra \
                    parcelada que cai no mês. A parcela traz a data da compra, o número e o total de \
                    parcelas, o id da compra, a situação e se o pagamento foi antecipado; a despesa não \
                    traz o campo parcela. O campo paga marca o item com pagamento registrado, seja ele \
                    despesa ou parcela.""")
    ResponseEntity<DetalheFaturaDTO> buscarFatura(
            @Parameter(description = "Id da fatura: o id do cartão seguido do mês, como em 3f1c...-2026-09")
            @PathVariable String id);

    @PostMapping("/{id}/pagamento")
    @Operation(
            summary = "Registra o pagamento de uma fatura",
            description = """
                    Marca como pago, com a data de hoje, tudo o que a fatura tem neste momento: as \
                    despesas lançadas no cartão durante o ciclo e as parcelas que caem no mês. Não há \
                    corpo na requisição, e a resposta é a fatura já recalculada.

                    Cada parcela em aberto gera uma despesa PAGA na conta de pagamento do cartão, como no \
                    pagamento de uma parcela só. As despesas lançadas direto no cartão não geram outra \
                    despesa, porque já contam na data da compra: o pagamento só debita o valor delas do \
                    saldo da conta. Sem conta de pagamento definida no cartão, o pagamento é recusado.

                    O pagamento vale para os itens, e não para o ciclo: uma compra que entrar depois numa \
                    fatura ainda aberta volta a contar no valorRestante. A fatura já fechada sem nada a \
                    pagar passa a PAGA; a aberta continua ABERTA, com valorRestante zero.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pagamento da fatura registrado com sucesso!"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Fatura não encontrada!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Fatura já paga, ou cartão sem conta de pagamento!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<FaturaCartaoDTO> registrarPagamentoFatura(
            @Parameter(description = "Id da fatura: o id do cartão seguido do mês, como em 3f1c...-2026-09")
            @PathVariable String id);

    @DeleteMapping("/{id}/pagamento")
    @Operation(
            summary = "Desfaz o pagamento de uma fatura",
            description = """
                    Remove todo pagamento registrado nos itens da fatura, inclusive o das parcelas \
                    marcadas uma a uma, e responde sem corpo. As despesas geradas pelas parcelas são \
                    removidas e tudo o que o pagamento debitou volta ao saldo da conta. A parcela que já \
                    venceu continua PAGA, porque passa a valer a data de vencimento.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Pagamento da fatura desfeito com sucesso!"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Fatura ou pagamento de fatura não encontrado!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<Void> deletarPagamentoFatura(
            @Parameter(description = "Id da fatura: o id do cartão seguido do mês, como em 3f1c...-2026-09")
            @PathVariable String id);
}