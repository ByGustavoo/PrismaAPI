package br.com.prismaapi.model.dto.dashboard.fatura;

import br.com.prismaapi.enums.SituacaoFatura;
import java.math.BigDecimal;

public record FaturaDTO(

        BigDecimal total,

        String nomeCartao,

        String dataVencimento,

        SituacaoFatura situacao

) {}