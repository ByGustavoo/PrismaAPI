package br.com.prismaapi.controller.aviso;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.aviso.AdiarAvisoDTO;
import br.com.prismaapi.model.dto.aviso.AvisoDTO;
import br.com.prismaapi.model.dto.aviso.PreferenciasAvisosDTO;
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

@Tag(name = "Aviso", description = "Endpoints relacionados aos avisos do sino do cabeçalho")
public interface AvisoDocs {

    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Avisos retornados com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @Operation(
            summary = "Lista os avisos",
            description = """
                    Retorna os avisos derivados dos dados das outras telas, sem cadastro próprio: faturas \
                    ainda não pagas que vencem dentro da antecedência escolhida ou venceram há até 15 dias, \
                    lançamentos pendentes ou agendados dentro da antecedência, inclusive os atrasados, \
                    recorrentes sem lançamento no mês, cartões de crédito a partir do percentual escolhido \
                    do limite, orçamentos do mês a partir de 80% do limite, o primeiro mês em que a \
                    previsão fica negativa e investimentos e metas sem atualização há mais tempo que o \
                    lembrete escolhido. Os tipos desligados nas preferências não aparecem.

                    A severidade é CRITICO para o que vence em até dois dias ou já venceu, para o cartão a \
                    partir de 90% do limite, para o orçamento estourado e para o saldo negativo no mês atual \
                    ou no seguinte; ATENCAO para o que vence em até sete dias, para o cartão abaixo de 90%, \
                    para o orçamento em alerta, para o saldo negativo mais adiante e para os lembretes; INFO \
                    para o resto. Lançamento agendado é sempre INFO.

                    Cada aviso traz o estado: NOVO, LIDO ou ADIADO (com adiadoAte). A marca vale enquanto a \
                    situação do aviso for a mesma — severidade, data e valor; mudou, ele volta a NOVO. O \
                    adiado volta a NOVO no dia de adiadoAte.""")
    ResponseEntity<List<AvisoDTO>> listarAvisos();

    @PostMapping("/{id}/leitura")
    @Operation(
            summary = "Marca um aviso como lido",
            description = """
                    Grava a leitura do aviso na situação em que ele está agora, sem corpo na requisição. Ele \
                    continua na lista com estado LIDO e volta a NOVO quando severidade, data ou valor mudam.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Leitura do aviso registrada com sucesso!"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Aviso não encontrado!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<AvisoDTO> registrarLeituraAviso(
            @Parameter(description = "Id do aviso, como em aviso-fatura-3f1c...-2026-10")
            @PathVariable String id);

    @PostMapping("/{id}/adiamento")
    @Operation(
            summary = "Adia um aviso",
            description = """
                    Tira o aviso dos novos por alguns dias. Ele fica ADIADO até a data de hoje somada aos \
                    dias informados e volta a NOVO nesse dia, ou antes, se a situação dele mudar.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Adiamento do aviso registrado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Aviso não encontrado!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<AvisoDTO> registrarAdiamentoAviso(
            @Parameter(description = "Id do aviso, como em aviso-fatura-3f1c...-2026-10")
            @PathVariable String id,
            @RequestBody @Valid AdiarAvisoDTO adiarAvisoDTO);

    @DeleteMapping("/{id}/leitura")
    @Operation(
            summary = "Devolve um aviso aos novos",
            description = "Apaga a leitura ou o adiamento do aviso e responde sem corpo. Sem marca nenhuma, não faz nada.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Aviso devolvido aos novos com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<Void> deletarLeituraAviso(
            @Parameter(description = "Id do aviso, como em aviso-fatura-3f1c...-2026-10")
            @PathVariable String id);

    @PostMapping("/leitura")
    @Operation(
            summary = "Marca todos os avisos novos como lidos",
            description = """
                    Grava a leitura de todo aviso NOVO, sem corpo na requisição, e devolve a lista inteira já \
                    com os estados novos. Os adiados continuam adiados. Marcas de avisos que já não existem \
                    são apagadas.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Leitura dos avisos registrada com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<List<AvisoDTO>> registrarLeituraAvisos();

    @DeleteMapping("/leitura")
    @Operation(
            summary = "Devolve todos os avisos aos novos",
            description = "Apaga todas as leituras e todos os adiamentos e responde sem corpo.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Avisos devolvidos aos novos com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<Void> deletarLeiturasAvisos();

    @GetMapping("/preferencias")
    @Operation(
            summary = "Busca as preferências dos avisos",
            description = """
                    Retorna os tipos de aviso desligados, a antecedência dos avisos com data (3, 7, 15 ou 30 \
                    dias), o percentual do limite do cartão que dispara o aviso (70, 80 ou 90) e os lembretes \
                    de investimento e de meta (15, 30, 60 ou 90 dias).""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Preferências dos avisos retornadas com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<PreferenciasAvisosDTO> buscarPreferenciasAvisos();

    @PutMapping("/preferencias")
    @Operation(
            summary = "Atualiza as preferências dos avisos",
            description = "Substitui as preferências inteiras. Valor fora das opções responde 400.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Preferências dos avisos atualizadas com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<PreferenciasAvisosDTO> atualizarPreferenciasAvisos(@RequestBody @Valid PreferenciasAvisosDTO preferenciasAvisosDTO);
}