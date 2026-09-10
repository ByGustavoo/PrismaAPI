package br.com.prismaapi.controller.relatorio;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.relatorio.RelatorioDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Relatório", description = "Endpoints relacionados ao relatório consolidado de um período")
public interface RelatorioDocs {

    @Operation(
            summary = "Relatório consolidado do período",
            description = """
                    Retorna, para o recorte de datas informado, receitas, despesas e resultado, as \
                    variações contra o intervalo anterior de mesma duração, os totais por categoria e por \
                    origem, o fluxo de caixa, a evolução do saldo e o patrimônio separado entre contas e \
                    investimentos. Recorte sem lançamento responde com as listas vazias.

                    O fluxo de caixa e a evolução do saldo são agrupados conforme a duração do recorte: por \
                    dia até 10 dias, por semana até 45 dias, rotulada pelo primeiro dia, e por mês acima \
                    disso. O saldo de cada grupo é o do último dia dele. O patrimônio cobre no mínimo seis \
                    meses. Transferências ficam fora da contagem de lançamentos e do gasto por origem.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Relatório retornado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Período inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/summary")
    ResponseEntity<RelatorioDTO> getResumo(
            @Parameter(description = "Início do recorte, inclusivo", example = "2026-09-01")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicial,

            @Parameter(description = "Fim do recorte, inclusivo", example = "2026-09-30")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFinal);
}