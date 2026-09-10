package br.com.prismaapi.model.dto.relatorio;

import br.com.prismaapi.enums.GrupoOrigem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Representa o gasto do período somado por conta ou cartão de origem.")
public record GastoOrigemDTO(

        UUID id,

        String nome,

        GrupoOrigem grupo,

        BigDecimal valor,

        BigDecimal participacao

) {}