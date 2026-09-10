package br.com.prismaapi.model.dto.conta;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoConta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de uma Conta.")
public record ContaDTO(

        UUID id,

        String nome,

        String instituicao,

        TipoConta tipo,

        BigDecimal saldo,

        Situacao situacao,

        Boolean incluirNoTotal

) {}