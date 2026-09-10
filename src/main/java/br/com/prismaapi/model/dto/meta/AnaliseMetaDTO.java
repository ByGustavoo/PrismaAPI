package br.com.prismaapi.model.dto.meta;

import br.com.prismaapi.enums.LeituraMeta;
import br.com.prismaapi.enums.Tendencia;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Representa a análise do histórico de preços de uma Meta.")
public record AnaliseMetaDTO(

        BigDecimal precoInicial,

        BigDecimal precoAtual,

        BigDecimal menorPreco,

        BigDecimal maiorPreco,

        BigDecimal precoMedio,

        BigDecimal variacao,

        BigDecimal variacaoPercentual,

        Tendencia tendencia,

        BigDecimal economia,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate ultimaAtualizacao,

        Integer quantidadeRegistros,

        LeituraMeta leitura

) {}