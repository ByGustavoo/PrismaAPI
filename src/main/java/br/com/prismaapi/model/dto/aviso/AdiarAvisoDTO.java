package br.com.prismaapi.model.dto.aviso;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Representa o modelo de requisição para adiar um aviso por alguns dias.")
public record AdiarAvisoDTO(

        @Min(value = 1, message = "O campo 'dias' deve ser de pelo menos 1 dia!")
        @Max(value = 30, message = "O campo 'dias' deve ser de no máximo 30 dias!")
        @NotNull(message = "O campo 'dias' é obrigatório!")
        Integer dias

) {}