package br.com.prismaapi.model.dto.dashboard.projection;

import br.com.prismaapi.enums.TipoLancamento;

import java.math.BigDecimal;

public record TotalMensalProjecao(

        Integer ano,

        Integer mes,

        TipoLancamento tipo,

        BigDecimal valor

) {}