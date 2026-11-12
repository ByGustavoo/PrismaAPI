package br.com.prismaapi.model.dto.investimento;

import br.com.prismaapi.enums.ClasseAtivo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de um Investimento.")
public record InvestimentoDTO(

        UUID id,

        String nome,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String codigo,

        ClasseAtivo classeAtivo,

        String instituicao,

        Boolean reservaEmergencia,

        BigDecimal aportado,

        BigDecimal valorAtual,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        BigDecimal quantidade,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        BigDecimal precoMedio,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        BigDecimal cotacao,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataInicio,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataAtualizacao,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String observacoes

) {}