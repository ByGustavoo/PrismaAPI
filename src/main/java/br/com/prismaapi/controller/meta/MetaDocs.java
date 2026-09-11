package br.com.prismaapi.controller.meta;

import br.com.prismaapi.enums.SituacaoMeta;
import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.meta.AtualizarMetaDTO;
import br.com.prismaapi.model.dto.meta.MetaDTO;
import br.com.prismaapi.model.dto.meta.ResumoMetasDTO;
import br.com.prismaapi.model.dto.meta.SalvarMetaDTO;
import br.com.prismaapi.model.dto.metapreco.SalvarMetaPrecoDTO;
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

@Tag(name = "Meta", description = "Endpoints relacionados às metas de compra e ao histórico de preços")
public interface MetaDocs {

    @Operation(
            summary = "Lista as metas",
            description = """
                    Retorna cada meta com o histórico de preços em ordem cronológica e a análise já \
                    calculada, da atualizada mais recentemente para a mais antiga, além dos totais.

                    A variação e a tendência são medidas contra o primeiro preço, e a tendência é \
                    ESTAVEL enquanto a variação fica dentro de 0,5%. A economia é o quanto o preço atual \
                    está abaixo do maior já registrado. A leitura é PRIMEIRO com um registro só, ESTAVEL \
                    quando todos os preços são iguais, MENOR ou MAIOR quando o preço atual está nos 5% \
                    das pontas da faixa e, fora delas, ABAIXO_DA_MEDIA ou ACIMA_DA_MEDIA. Os totais somam \
                    só as metas em acompanhamento. A busca casa com o nome e a observação, sem \
                    diferenciar maiúscula nem acento.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Metas retornadas com sucesso!"),
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
    ResponseEntity<ResumoMetasDTO> listarMetas(
            @Parameter(description = "Situação da meta", example = "ACOMPANHANDO")
            @RequestParam(required = false) SituacaoMeta situacao,

            @Parameter(description = "Texto buscado no nome e na observação", example = "notebook")
            @RequestParam(required = false) String busca);

    @Operation(
            summary = "Cadastra uma meta",
            description = """
                    Cadastra a meta junto com o primeiro preço, que vira o início do histórico e a data \
                    de criação da meta.

                    Os links são opcionais, mas, se vierem, precisam começar com http:// ou https://. O \
                    servidor nunca acessa esses endereços. Nome, links e observações são gravados sem os \
                    espaços das pontas.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Meta cadastrada com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    ResponseEntity<MetaDTO> salvarMeta(@RequestBody @Valid SalvarMetaDTO salvarMetaDTO);

    @Operation(
            summary = "Atualiza uma meta",
            description = """
                    Substitui nome, links, situação e observações pelo corpo enviado e devolve a meta com \
                    o histórico completo.

                    A edição não mexe em preço: o histórico e a data de criação são preservados, e preço \
                    novo é sempre um registro novo. Links e observações que não vierem são limpos.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Meta atualizada com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição ou id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Meta não encontrada!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PutMapping("/{id}")
    ResponseEntity<MetaDTO> atualizarMeta(
            @Parameter(description = "Id da meta")
            @PathVariable UUID id,

            @RequestBody @Valid AtualizarMetaDTO atualizarMetaDTO);

    @Operation(
            summary = "Registra um preço da meta",
            description = """
                    Acrescenta um preço ao histórico, sem nunca substituir o anterior, e devolve a meta \
                    com a série atualizada.

                    A data não pode estar no futuro nem ser anterior ao primeiro preço, que é a \
                    referência de toda a variação. O mesmo preço na mesma data é recusado como registro \
                    repetido.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Preço registrado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição ou id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Meta não encontrada!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Já existe um registro com esse preço nesta data!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Data anterior ao primeiro preço!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/{id}/precos")
    ResponseEntity<MetaDTO> registrarPreco(
            @Parameter(description = "Id da meta")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarMetaPrecoDTO salvarMetaPrecoDTO);

    @Operation(
            summary = "Exclui uma meta",
            description = """
                    Remove a meta e todo o histórico de preços dela, de forma definitiva, e responde sem \
                    corpo. É a única operação destrutiva do domínio.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Meta excluída com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Meta não encontrada!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deletarMeta(
            @Parameter(description = "Id da meta")
            @PathVariable UUID id);
}