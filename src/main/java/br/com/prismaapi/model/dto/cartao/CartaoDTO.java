package br.com.prismaapi.model.dto.cartao;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoCartao;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de um Cartão.")
public record CartaoDTO(

        UUID id,

        String nome,

        String instituicao,

        TipoCartao tipo,

        Situacao situacao,

        String bandeira,

        String ultimosDigitos,

        BigDecimal limiteCredito,

        Short diaFechamento,

        Short diaVencimento,

        UUID contaId,

        BigDecimal saldo,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataCriacao,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataAtualizacao

) {}
