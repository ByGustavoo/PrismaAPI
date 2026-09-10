package br.com.prismaapi.model.dto.fatura;

import br.com.prismaapi.enums.SituacaoFatura;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Representa a fatura de um cartão de crédito num mês, com os itens que a compõem.")
public record DetalheFaturaDTO(

        String id,

        UUID idCartao,

        String nomeCartao,

        String mes,

        BigDecimal total,

        SituacaoFatura situacao,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataFechamento,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataVencimento,

        Integer quantidadeItens,

        BigDecimal totalAnterior,

        List<ItemFaturaDTO> itens

) {}