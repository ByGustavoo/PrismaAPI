package br.com.prismaapi.model.dto.dashboard.projection;

import br.com.prismaapi.enums.TipoLancamento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimentoDiarioProjecao(

        LocalDate data,

        TipoLancamento tipo,

        BigDecimal valor

) {}