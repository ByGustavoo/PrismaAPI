package br.com.prismaapi.controller.dashboard;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.dashboard.DashboardDTO;
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

import java.time.YearMonth;

@Tag(name = "Dashboard", description = "Endpoints relacionados ao resumo da tela inicial")
public interface DashboardDocs {

    @Operation(
            summary = "Resumo consolidado do dashboard",
            description = """
                    Retorna, numa só resposta, os totais do período, as variações contra a janela \
                    anterior de mesmo tamanho e as quatro séries que alimentam os gráficos.

                    Sem os dois parâmetros, responde pelo mês corrente. Eles andam juntos: enviar \
                    apenas um é requisição inválida. Num recorte de mês único as séries de histórico \
                    de saldo, fluxo de caixa e gasto diário abrem para seis meses, porque um mês \
                    sozinho não desenha linha nenhuma.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumo do dashboard retornado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Período inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/resumo")
    ResponseEntity<DashboardDTO> buscarResumo(
            @Parameter(description = "Primeiro mês do recorte", example = "2026-09")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth dataInicial,

            @Parameter(description = "Último mês do recorte, inclusivo", example = "2026-09")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth dataFinal);
}