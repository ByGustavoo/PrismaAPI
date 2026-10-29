package br.com.prismaapi.model.dto.aviso;

import br.com.prismaapi.enums.TipoAviso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Representa as preferências dos avisos do sino: tipos desligados, antecedência e lembretes.")
public record PreferenciasAvisosDTO(

        @NotNull(message = "O campo 'tiposDesligados' é obrigatório!")
        List<TipoAviso> tiposDesligados,

        @NotNull(message = "O campo 'diasAntecedencia' é obrigatório!")
        Integer diasAntecedencia,

        @NotNull(message = "O campo 'percentualLimiteCartao' é obrigatório!")
        Integer percentualLimiteCartao,

        @NotNull(message = "O campo 'diasLembreteInvestimento' é obrigatório!")
        Integer diasLembreteInvestimento,

        @NotNull(message = "O campo 'diasLembreteMeta' é obrigatório!")
        Integer diasLembreteMeta

) {}