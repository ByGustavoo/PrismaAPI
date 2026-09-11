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

        ClasseAtivo classeAtivo,

        String instituicao,

        BigDecimal aportado,

        BigDecimal valorAtual,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataInicio,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String observacoes

) {}