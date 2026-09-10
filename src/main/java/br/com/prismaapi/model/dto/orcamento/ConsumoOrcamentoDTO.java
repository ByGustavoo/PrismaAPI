package br.com.prismaapi.model.dto.orcamento;

import br.com.prismaapi.enums.SituacaoOrcamento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Representa o consumo de um orçamento no mês consultado.")
public record ConsumoOrcamentoDTO(

        OrcamentoDTO orcamento,

        BigDecimal gasto,

        BigDecimal restante,

        BigDecimal consumo,

        BigDecimal projecao,

        SituacaoOrcamento situacao

) {}