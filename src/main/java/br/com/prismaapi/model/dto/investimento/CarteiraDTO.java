package br.com.prismaapi.model.dto.investimento;

import br.com.prismaapi.model.dto.dashboard.variacao.VariacaoDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Representa a carteira de investimentos consolidada.")
public record CarteiraDTO(

        BigDecimal aportado,

        BigDecimal valorAtual,

        BigDecimal rendimento,

        BigDecimal rentabilidade,

        VariacaoDTO variacaoValorAtual,

        List<AlocacaoDTO> alocacao,

        List<EvolucaoCarteiraDTO> historico,

        List<PosicaoDTO> posicoes

) {}