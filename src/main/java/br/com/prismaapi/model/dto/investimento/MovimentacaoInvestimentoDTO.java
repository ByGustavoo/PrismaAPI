package br.com.prismaapi.model.dto.investimento;

import br.com.prismaapi.enums.TipoMovimentacaoInvestimento;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Representa uma movimentação de um Investimento, com o saldo e o aportado logo depois dela.")
public record MovimentacaoInvestimentoDTO(

        UUID id,

        TipoMovimentacaoInvestimento tipo,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate data,

        BigDecimal valor,

        BigDecimal saldoApos,

        BigDecimal aportadoApos,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String descricao

) {}