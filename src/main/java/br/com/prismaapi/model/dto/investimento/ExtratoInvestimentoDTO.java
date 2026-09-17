package br.com.prismaapi.model.dto.investimento;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Representa o extrato de um Investimento: a posição, as movimentações e a evolução mensal.")
public record ExtratoInvestimentoDTO(

        PosicaoDTO posicao,

        Integer quantidadeAportes,

        @JsonInclude(JsonInclude.Include.ALWAYS)
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate ultimoAporte,

        List<MovimentacaoInvestimentoDTO> movimentacoes,

        List<PontoEvolucaoDTO> evolucao

) {}