package br.com.prismaapi.model.dto.investimento;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Representa o modelo de requisição para registrar a compra de ações.")
public record SalvarCompraAcaoDTO(

        @NotNull(message = "O campo 'quantidade' é obrigatório!")
        @Positive(message = "O campo 'quantidade' deve ser maior que zero!")
        @Digits(integer = 10, fraction = 0, message = "O campo 'quantidade' deve ser um número inteiro de ações!")
        BigDecimal quantidade,

        @NotNull(message = "O campo 'precoUnitario' é obrigatório!")
        @Positive(message = "O campo 'precoUnitario' deve ser maior que zero!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'precoUnitario' deve ter no máximo duas casas decimais!")
        BigDecimal precoUnitario,

        @PositiveOrZero(message = "O campo 'taxas' não pode ser negativo!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'taxas' deve ter no máximo duas casas decimais!")
        BigDecimal taxas,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "O campo 'data' é obrigatório!")
        @PastOrPresent(message = "O campo 'data' não pode estar no futuro!")
        LocalDate data,

        UUID idConta,

        @Size(max = 160, message = "O campo 'descricao' deve ter no máximo 160 caracteres!")
        String descricao

) {}