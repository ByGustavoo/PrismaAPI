package br.com.prismaapi.model.dto.lancamento;

import br.com.prismaapi.enums.TipoMovimentacaoInvestimento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Identifica o aporte ou o resgate de investimento que gerou o Lançamento.")
public record InvestimentoLancamentoDTO(

        UUID idInvestimento,

        TipoMovimentacaoInvestimento tipo

) {}