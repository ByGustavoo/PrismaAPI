package br.com.prismaapi.model.dto.dashboard.gasto.diario;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public record GastoDiarioDTO(

        @JsonFormat(pattern = "yyyy-MM-dd")
        String data,

        BigDecimal valor

) {}