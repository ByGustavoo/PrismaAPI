package br.com.prismaapi.model.dto.investimento;

import br.com.prismaapi.enums.ClasseAtivo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de um Investimento.")
public record InvestimentoDTO(

        UUID id,

        String nome,

        ClasseAtivo classeAtivo,

        String instituicao,

        BigDecimal aportado,

        BigDecimal valorAtual,

        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dataInicio,

        String observacoes,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataCriacao,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataAtualizacao

) {}
