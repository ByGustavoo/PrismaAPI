package br.com.prismaapi.model.dto.dashboard.projection;

import java.math.BigDecimal;

public record CarteiraProjecao(

        BigDecimal valorAtual,

        BigDecimal aportado

) {}