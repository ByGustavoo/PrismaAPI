package br.com.prismaapi.model.dto.relatorio.projection;

import java.math.BigDecimal;
import java.util.UUID;

public record GastoOrigemProjecao(

        UUID idConta,

        String nomeConta,

        UUID idCartao,

        String nomeCartao,

        BigDecimal valor

) {}