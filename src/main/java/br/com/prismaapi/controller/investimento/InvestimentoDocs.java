package br.com.prismaapi.controller.investimento;

import br.com.prismaapi.exceptions.dto.ErrorResponseDTO;
import br.com.prismaapi.model.dto.investimento.AtualizarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.CarteiraDTO;
import br.com.prismaapi.model.dto.investimento.ExtratoInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.InvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarAcaoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarAporteInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarCompraAcaoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarCotacaoAcaoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarResgateInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarSaldoInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarVendaAcaoDTO;
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

    @GetMapping("/carteira")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Carteira retornada com sucesso!"),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
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
                    para o menor.

                    A reserva soma os investimentos marcados como reserva de emergência, de qualquer \
                    classe de ativo, e vem zerada quando nenhum está marcado.""")
    ResponseEntity<CarteiraDTO> buscarCarteira();

    @GetMapping("/{id}/extrato")
    @Operation(
            summary = "Extrato de um investimento",
            description = """
                    Retorna a posição do investimento, as movimentações da mais recente para a mais \
                    antiga e a evolução mensal.

                    Um aporte soma ao saldo e ao aportado. Um resgate tira do saldo, com valor exibido \
                    negativo, e tira do aportado na mesma proporção do saldo que saiu, para a \
                    rentabilidade da posição não mudar com a retirada. Um saldo informado substitui o \
                    saldo, e o valor exibido dele é a diferença para o saldo anterior, que pode ser \
                    negativa. Aporte e resgate ligados a uma conta trazem idConta e nomeConta. \
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
    ResponseEntity<ExtratoInvestimentoDTO> buscarExtrato(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id);

    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Investimento cadastrado com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Ação cadastrada por valor ou conta inexistente!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @Operation(
            summary = "Cadastra um investimento",
            description = """
                    Cadastra o investimento e devolve o registro salvo. O aportado é a aplicação inicial \
                    e o valor atual, o saldo de hoje: o servidor grava um aporte do aportado na data de \
                    início, com a descrição "Aplicação inicial", e, se o valor atual for diferente, um \
                    saldo informado na data de hoje, com a descrição "Saldo informado no cadastro".

                    Com idConta, a aplicação inicial sai dessa conta: o servidor grava uma transferência \
                    paga da conta para o investimento, na data de início, e o saldo da conta baixa. Sem \
                    ele, o dinheiro já estava investido e nenhuma conta muda. reservaEmergencia marca o \
                    investimento como parte da reserva de emergência, em qualquer classe.

                    Valor atual abaixo do aportado é aceito, porque posição no prejuízo existe. A data \
                    da aplicação inicial não pode estar no futuro. Nome, instituição e observações são \
                    gravados sem os espaços das pontas. Ações não são aceitas aqui: elas são cadastradas \
                    pela quantidade, em POST /investimentos/acoes.""")
    ResponseEntity<InvestimentoDTO> salvarInvestimento(@RequestBody @Valid SalvarInvestimentoDTO salvarInvestimentoDTO);

    @PostMapping("/acoes")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Ação cadastrada com sucesso!"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Corpo da requisição inválido!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Conta inexistente!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @Operation(
            summary = "Cadastra uma ação pela quantidade",
            description = """
                    Cadastra uma ação na classe ACOES e devolve o registro salvo. O servidor grava a \
                    primeira compra na data de início, com a descrição "Primeira compra": o aportado é \
                    quantidade vezes preço por ação mais as taxas, e o saldo, quantidade vezes o preço. \
                    Com cotacao diferente do preço, ele grava também a cotação na data de hoje, com a \
                    descrição "Cotação informada no cadastro", e o saldo passa a ser quantidade vezes \
                    a cotação.

                    O código é gravado em maiúsculas. idConta, reservaEmergencia e as demais regras são \
                    as do cadastro por valor. A resposta traz quantidade, precoMedio (aportado dividido \
                    pela quantidade) e cotacao.""")
    ResponseEntity<InvestimentoDTO> salvarAcao(@RequestBody @Valid SalvarAcaoDTO salvarAcaoDTO);

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualiza um investimento",
            description = """
                    Substitui nome, classe, instituição, a marca de reserva de emergência e observações. \
                    Os valores não são editáveis: \
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
    ResponseEntity<InvestimentoDTO> atualizarInvestimento(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id,

            @RequestBody @Valid AtualizarInvestimentoDTO atualizarInvestimentoDTO);

    @PostMapping("/{id}/aportes")
    @Operation(
            summary = "Registra um aporte num investimento",
            description = """
                    Grava um aporte, dinheiro novo, e devolve o investimento já atualizado: o valor soma \
                    ao saldo e ao aportado.

                    A data não pode estar no futuro nem ser anterior à data da última movimentação, \
                    porque um aporte anterior ao último saldo informado seria engolido por ele.

                    Com idConta, o dinheiro sai dessa conta: o servidor grava uma transferência paga \
                    da conta para o investimento, com a descrição "Aporte — <nome>", e o saldo da conta \
                    baixa. Essa transferência não se edita nem se exclui pelos lançamentos.""")
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
                    description = "Data anterior à última atualização ou conta inexistente!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<InvestimentoDTO> registrarAporte(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarAporteInvestimentoDTO salvarAporteInvestimentoDTO);

    @PostMapping("/{id}/resgates")
    @Operation(
            summary = "Registra um resgate num investimento",
            description = """
                    Grava um resgate, dinheiro que sai do investimento, e devolve o investimento já \
                    atualizado: o valor sai do saldo, e o aportado diminui na mesma proporção, então \
                    resgatar metade do saldo deixa metade do aportado e a rentabilidade igual.

                    O resgate não pode passar do saldo atual, e a data segue a mesma regra do aporte. \
                    Com idConta, o dinheiro volta para essa conta: o servidor grava uma transferência \
                    paga do investimento para a conta, com a descrição "Resgate — <nome>", e o saldo da \
                    conta sobe.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Resgate registrado com sucesso!"),
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
                    description = "Resgate maior que o saldo, data anterior à última atualização ou conta inexistente!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<InvestimentoDTO> registrarResgate(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarResgateInvestimentoDTO salvarResgateInvestimentoDTO);

    @PostMapping("/{id}/saldos")
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
    ResponseEntity<InvestimentoDTO> registrarSaldo(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarSaldoInvestimentoDTO salvarSaldoInvestimentoDTO);

    @PostMapping("/{id}/compras")
    @Operation(
            summary = "Registra a compra de ações",
            description = """
                    Grava uma compra e devolve a ação já atualizada. A quantidade soma à posição, o \
                    aportado soma quantidade vezes preço mais as taxas, e o preço da compra passa a ser \
                    a cotação: o saldo é a quantidade nova vezes esse preço.

                    Só vale para ações com a quantidade informada; nos demais investimentos responde \
                    422. A data segue a regra do aporte, e com idConta o servidor grava a \
                    transferência "Compra — <código>" da conta para a ação.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Compra registrada com sucesso!"),
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
                    description = "Investimento sem quantidade, data anterior à última atualização ou conta inexistente!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<InvestimentoDTO> registrarCompra(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarCompraAcaoDTO salvarCompraAcaoDTO);

    @PostMapping("/{id}/vendas")
    @Operation(
            summary = "Registra a venda de ações",
            description = """
                    Grava uma venda e devolve a ação já atualizada. A quantidade sai da posição, o \
                    aportado diminui pelo preço médio das ações vendidas (na mesma proporção da \
                    quantidade) e o preço da venda passa a ser a cotação. O valor recebido é quantidade \
                    vezes preço menos as taxas.

                    A venda não passa da quantidade que se tem, e as taxas não passam do valor da \
                    venda. Com idConta, o valor recebido volta para a conta pela transferência \
                    "Venda — <código>".""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Venda registrada com sucesso!"),
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
                    description = "Venda maior que a quantidade, taxas maiores que a venda, investimento sem quantidade, data anterior à última atualização ou conta inexistente!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<InvestimentoDTO> registrarVenda(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarVendaAcaoDTO salvarVendaAcaoDTO);

    @PostMapping("/{id}/cotacoes")
    @Operation(
            summary = "Informa a cotação de uma ação",
            description = """
                    Grava a cotação e devolve a ação já atualizada: o saldo passa a ser a quantidade \
                    vezes a cotação, e a diferença para o saldo anterior aparece no extrato como \
                    rendimento. A data segue a regra do saldo.""")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cotação registrada com sucesso!"),
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
                    description = "Investimento sem quantidade ou data anterior à última atualização!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor!",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    ResponseEntity<InvestimentoDTO> registrarCotacao(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id,

            @RequestBody @Valid SalvarCotacaoAcaoDTO salvarCotacaoAcaoDTO);

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Exclui um investimento",
            description = """
                    Remove o investimento e as movimentações dele de forma definitiva e responde sem corpo.

                    As transferências que aportes e resgates gravaram nas contas saem junto, e o saldo \
                    dessas contas é recalculado como se elas nunca tivessem existido. A posição sai da \
                    carteira, da alocação e da evolução na próxima leitura.""")
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
    ResponseEntity<Void> deletarInvestimento(
            @Parameter(description = "Id do investimento")
            @PathVariable UUID id);
}