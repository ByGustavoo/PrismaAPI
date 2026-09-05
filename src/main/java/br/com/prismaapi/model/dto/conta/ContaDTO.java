package br.com.prismaapi.model.dto.conta;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoConta;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de uma Conta.")
public record ContaDTO(

        UUID id,

        String nome,

        String instituicao,

        TipoConta tipo,

        BigDecimal saldo,

        Situacao situacao,

        Boolean incluirNoTotal,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataCriacao,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataAtualizacao

) {}
