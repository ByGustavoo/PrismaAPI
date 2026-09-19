package br.com.prismaapi.model.dto.metapreco;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Representa o modelo de requisição para registrar um Preço de Meta.")
public record SalvarMetaPrecoDTO(

        @NotNull(message = "O campo 'preco' é obrigatório!")
        @Positive(message = "O campo 'preco' deve ser maior que zero!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'preco' deve ter no máximo duas casas decimais!")
        BigDecimal preco,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "O campo 'data' é obrigatório!")
        @PastOrPresent(message = "O campo 'data' não pode estar no futuro!")
        LocalDate data,

        @Size(max = 500, message = "O campo 'observacao' deve ter no máximo 500 caracteres!")
        String observacao

) {}