package br.com.prismaapi.controller.investimento;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.investimento.CarteiraDTO;
import br.com.prismaapi.model.dto.investimento.InvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarInvestimentoDTO;
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

@Tag(name = "Investimento", description = "Endpoints relacionados aos investimentos e à carteira consolidada")
public interface InvestimentoDocs {

    @Operation(
            summary = "Lista os investimentos",
            description = """
                    Retorna os investimentos como estão cadastrados, sem os números da carteira: do maior \
                    valor atual para o menor e, no empate, em ordem alfabética pelo nome.

                    É a lista que alimenta o formulário de edição. A tela da carteira usa o resumo \
                    consolidado, que já traz rendimento, rentabilidade e participação de cada posição.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Investimentos retornados com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    ResponseEntity<List<InvestimentoDTO>> listarInvestimentos();

    @Operation(
            summary = "Resumo consolidado da carteira",
            description = """
                    Retorna os totais da carteira, a distribuição por classe de ativo, a evolução dos \
                    últimos doze meses e cada posição com rendimento, rentabilidade e participação já \
                    calculados. Rentabilidade e participação são frações de 0 a 1; a variação do valor \
                    atual é em pontos percentuais, contra o patrimônio do mês anterior.

                    Não há histórico de cotação. A evolução distribui o aporte de cada posição \
                    linearmente entre o mês do primeiro aporte e o mês corrente, e o rendimento aparece \
                    aos poucos, inteiro só no mês corrente: a posição nasce valendo o que foi aportado e \
                    o último ponto fecha exatamente com o valor atual. A alocação traz só as classes com \
                    posição e, como as posições, vem do maior valor atual para o menor.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Carteira retornada com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/carteira")
    ResponseEntity<CarteiraDTO> buscarCarteira();

    @Operation(
            summary = "Cadastra um investimento",
            description = """
                    Cadastra o investimento e devolve o registro salvo.

                    Valor atual abaixo do aportado é aceito, porque posição no prejuízo existe. A data \
                    do primeiro aporte não pode estar no futuro, já que é ela que dá idade à posição na \
                    evolução da carteira. Nome, instituição e observações são gravados sem os espaços \
                    das pontas.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Investimento cadastrado com sucesso!"),
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
    ResponseEntity<InvestimentoDTO> salvarInvestimento(@RequestBody @Valid SalvarInvestimentoDTO salvarInvestimentoDTO);

    @Operation(
            summary = "Atualiza um investimento",
            description = """
                    Substitui o investimento inteiro pelo corpo enviado, com as mesmas regras do cadastro.

                    Observações que não vierem no corpo são limpas, e não preservadas. Mudar a data do \
                    primeiro aporte refaz a curva de evolução da carteira na próxima leitura.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Investimento atualizado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição ou id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Investimento não encontrado!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PutMapping("/{id}")
    ResponseEntity<InvestimentoDTO> atualizarInvestimento(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarInvestimentoDTO salvarInvestimentoDTO);

    @Operation(
            summary = "Exclui um investimento",
            description = """
                    Remove o investimento de forma definitiva e responde sem corpo.

                    Nenhum outro registro aponta para um investimento, então a exclusão não é recusada \
                    por histórico: a posição sai da carteira, da alocação e da evolução na próxima leitura.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Investimento excluído com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Investimento não encontrado!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deletarInvestimento(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id);
}