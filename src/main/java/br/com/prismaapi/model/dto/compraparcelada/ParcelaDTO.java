package br.com.prismaapi.model.dto.compraparcelada;

import br.com.prismaapi.enums.SituacaoParcela;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Representa uma parcela do cronograma de uma Compra Parcelada.")
public record ParcelaDTO(

        Integer numero,

        String mes,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataVencimento,

        BigDecimal valor,

        SituacaoParcela situacao

) {}