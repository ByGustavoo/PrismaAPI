package br.com.prismaapi.model.dto.dashboard.projection;

import java.math.BigDecimal;

public record ResumoDoPeriodoProjecao(

        BigDecimal receitas,

        BigDecimal despesas,

        BigDecimal receitasAnteriores,

        BigDecimal despesasAnteriores,

        Long quantidade

) {}