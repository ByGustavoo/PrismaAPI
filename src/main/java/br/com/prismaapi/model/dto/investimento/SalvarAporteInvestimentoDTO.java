package br.com.prismaapi.model.dto.investimento;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Representa o modelo de requisição para registrar um aporte num Investimento.")
public record SalvarAporteInvestimentoDTO(

        @NotNull(message = "O campo 'valor' é obrigatório!")
        @Positive(message = "O campo 'valor' deve ser maior que zero!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'valor' deve ter no máximo duas casas decimais!")
        BigDecimal valor,

        @NotNull(message = "O campo 'data' é obrigatório!")
        @PastOrPresent(message = "O campo 'data' não pode estar no futuro!")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate data,

        @Size(max = 160, message = "O campo 'descricao' deve ter no máximo 160 caracteres!")
        String descricao

) {}