package br.com.prismaapi.model.dto.tela;

import br.com.prismaapi.enums.Tela;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Representa as telas ocultas do menu lateral, na ordem em que aparecem nele.")
public record TelasOcultasDTO(

        @NotNull(message = "O campo 'telas' é obrigatório!")
        List<@NotNull(message = "O campo 'telas' não aceita item vazio!") Tela> telas

) {}