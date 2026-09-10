package br.com.prismaapi.model.dto.meta;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Representa as metas com as análises e os totais das que estão em acompanhamento.")
public record ResumoMetasDTO(

        List<AcompanhamentoMetaDTO> itens,

        Integer quantidadeAcompanhando,

        Integer quantidadeCompradas,

        BigDecimal totalAtual,

        BigDecimal totalInicial,

        BigDecimal variacaoTotal,

        BigDecimal economiaTotal

) {}