package br.com.prismaapi.model.dto.cartao.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DespesaCartaoProjecao(

        UUID cartaoId,

        LocalDate data,

        BigDecimal valor,

        Long quantidade

) {}