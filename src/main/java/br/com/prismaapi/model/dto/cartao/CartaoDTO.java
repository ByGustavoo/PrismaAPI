package br.com.prismaapi.model.dto.cartao;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoCartao;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
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

        BigDecimal limiteComprometido,

        Short diaFechamento,

        Short diaVencimento,

        UUID idConta,

        String nomeConta,

        BigDecimal saldo

) {}