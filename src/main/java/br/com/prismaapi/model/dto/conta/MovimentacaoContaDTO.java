package br.com.prismaapi.model.dto.conta;

import br.com.prismaapi.enums.TipoMovimentacaoConta;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Representa um lançamento pago lido como movimentação de uma Conta, com o saldo logo depois dele.")
public record MovimentacaoContaDTO(

        UUID id,

        TipoMovimentacaoConta tipo,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate data,

        String descricao,

        BigDecimal valor,

        BigDecimal saldoApos

) {}