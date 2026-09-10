package br.com.prismaapi.model.dto.aviso;

import br.com.prismaapi.enums.SeveridadeAviso;
import br.com.prismaapi.enums.TipoAviso;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Representa um aviso derivado de faturas, lançamentos e limites de cartão.")
public record AvisoDTO(

        String id,

        TipoAviso tipo,

        SeveridadeAviso severidade,

        String titulo,

        String descricao,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate data,

        BigDecimal valor,

        String rota

) {}