package br.com.prismaapi.controller.investimento;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.investimento.AtualizarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.CarteiraDTO;
import br.com.prismaapi.model.dto.investimento.ExtratoInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.InvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarAporteInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarSaldoInvestimentoDTO;
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

@Tag(name = "Investimento", description = "Endpoints relacionados aos investimentos e à carteira consolidada")
public interface InvestimentoDocs {

    @Operation(
            summary = "Resumo consolidado da carteira",
            description = """
                    Retorna os totais da carteira, a distribuição por classe de ativo, a evolução dos \
                    últimos doze meses e cada posição com rendimento, rentabilidade e participação já \
                    calculados. Rentabilidade e participação são frações de 0 a 1; a variação do valor \
                    atual é em pontos percentuais, contra o patrimônio do mês anterior.

                    A evolução sai das movimentações: em cada fim de mês (hoje, no mês corrente), cada \
                    posição vale o saldo depois da última movimentação até ali. Ela começa no mês da \
                    primeira movimentação da carteira, com no mínimo dois e no máximo doze pontos. A \
                    alocação traz só as classes com posição e, como as posições, vem do maior valor atual \
                    para o menor.""")
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
            summary = "Extrato de um investimento",
            description = """
                    Retorna a posição do investimento, as movimentações da mais recente para a mais \
                    antiga e a evolução mensal.

                    Um aporte soma ao saldo e ao aportado. Um saldo informado substitui o saldo, e o \
                    valor exibido dele é a diferença para o saldo anterior, que pode ser negativa. \
                    saldoApos e aportadoApos são calculados na ordem da série: data e, no empate, \
                    ordem de registro. A evolução tem um ponto por mês desde o mês da primeira \
                    movimentação, com no mínimo dois e no máximo doze pontos.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Extrato retornado com sucesso!"),
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
    @GetMapping("/{id}/extrato")
    ResponseEntity<ExtratoInvestimentoDTO> buscarExtrato(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id);

    @Operation(
            summary = "Cadastra um investimento",
            description = """
                    Cadastra o investimento e devolve o registro salvo. O aportado é a aplicação inicial \
                    e o valor atual, o saldo de hoje: o servidor grava um aporte do aportado na data de \
                    início, com a descrição "Aplicação inicial", e, se o valor atual for diferente, um \
                    saldo informado na data de hoje, com a descrição "Saldo informado no cadastro".

                    Valor atual abaixo do aportado é aceito, porque posição no prejuízo existe. A data \
                    da aplicação inicial não pode estar no futuro. Nome, instituição e observações são \
                    gravados sem os espaços das pontas.""")
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
                    Substitui nome, classe, instituição e observações. Os valores não são editáveis: \
                    aportado, valor atual e datas saem das movimentações, e mudá-los à mão apagaria a \
                    história da série. Para mexer neles, registre um aporte ou um saldo.

                    Observações que não vierem no corpo são limpas, e não preservadas.""")
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

            @RequestBody @Valid AtualizarInvestimentoDTO atualizarInvestimentoDTO);

    @Operation(
            summary = "Registra um aporte num investimento",
            description = """
                    Grava um aporte, dinheiro novo, e devolve o investimento já atualizado: o valor soma \
                    ao saldo e ao aportado.

                    A data não pode estar no futuro nem ser anterior à data da última movimentação, \
                    porque um aporte anterior ao último saldo informado seria engolido por ele.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Aporte registrado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição ou id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Investimento não encontrado!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Data anterior à última atualização!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/{id}/aportes")
    ResponseEntity<InvestimentoDTO> registrarAporte(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarAporteInvestimentoDTO salvarAporteInvestimentoDTO);

    @Operation(
            summary = "Informa o saldo atual de um investimento",
            description = """
                    Grava um rendimento a partir do saldo informado e devolve o investimento já \
                    atualizado: o saldo passa a ser o valor informado e o aportado não muda.

                    A data não pode estar no futuro nem ser anterior à data da última movimentação.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Saldo registrado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição ou id inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Investimento não encontrado!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Data anterior à última atualização!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/{id}/saldos")
    ResponseEntity<InvestimentoDTO> registrarSaldo(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarSaldoInvestimentoDTO salvarSaldoInvestimentoDTO);

    @Operation(
            summary = "Exclui um investimento",
            description = """
                    Remove o investimento e as movimentações dele de forma definitiva e responde sem corpo.

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