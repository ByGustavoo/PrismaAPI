package br.com.prismaapi.model.dto.dashboard.fluxo;

import java.math.BigDecimal;

public record FluxoDTO(

        String rotulo,

        BigDecimal receitas,

        BigDecimal despesas

) {}