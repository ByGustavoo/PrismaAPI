package br.com.prismaapi.model.dto.orcamento;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Representa o modelo de requisição para salvar um Orçamento.")
public record SalvarOrcamentoDTO(

        @NotNull(message = "O campo 'idCategoria' é obrigatório!")
        UUID idCategoria,

        @NotNull(message = "O campo 'limiteMensal' é obrigatório!")
        @Positive(message = "O campo 'limiteMensal' deve ser maior que zero!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'limiteMensal' deve ter no máximo duas casas decimais!")
        BigDecimal limiteMensal

) {}