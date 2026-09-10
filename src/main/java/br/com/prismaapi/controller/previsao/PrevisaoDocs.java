package br.com.prismaapi.controller.previsao;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.previsao.PrevisaoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Previsão", description = "Endpoints relacionados à projeção do saldo nos próximos meses")
public interface PrevisaoDocs {

    @Operation(
            summary = "Projeta o saldo dos próximos meses",
            description = """
                    Parte do saldo total de hoje e projeta mês a mês, a partir do mês que vem, a receita, \
                    as despesas e o saldo no fim de cada mês, apontando o mês de menor saldo.

                    A receita é a média dos três meses fechados. As despesas recorrentes entram no mês \
                    exato em que vencem e as parcelas no mês da fatura em que caem. O gasto variável é a \
                    média de despesa dos três meses fechados menos as recorrentes médias do mesmo período, \
                    nunca negativo. As parcelas não são descontadas: elas não são lançamentos e nunca \
                    entram nessa média.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Previsão retornada com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Horizonte inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    ResponseEntity<PrevisaoDTO> getPrevisao(
            @Parameter(description = "Quantidade de meses projetados, de 1 a 24; sem ele, 6", example = "6")
            @RequestParam(required = false) Integer meses);
}