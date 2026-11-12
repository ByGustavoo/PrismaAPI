package br.com.prismaapi.model.dto.investimento;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Representa o modelo de requisição para cadastrar uma ação pela quantidade comprada e pelo preço por ação.")
public record SalvarAcaoDTO(

        @NotBlank(message = "O campo 'codigo' é obrigatório!")
        @Pattern(regexp = "^[A-Za-z0-9]{4,12}$", message = "O campo 'codigo' deve ter de 4 a 12 letras e números, como PETR4!")
        String codigo,

        @NotBlank(message = "O campo 'nome' é obrigatório!")
        @Size(min = 2, max = 120, message = "O campo 'nome' deve ter entre 2 e 120 caracteres!")
        String nome,

        @NotBlank(message = "O campo 'instituicao' é obrigatório!")
        @Size(min = 2, max = 80, message = "O campo 'instituicao' deve ter entre 2 e 80 caracteres!")
        String instituicao,

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

        @Positive(message = "O campo 'cotacao' deve ser maior que zero!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'cotacao' deve ter no máximo duas casas decimais!")
        BigDecimal cotacao,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "O campo 'dataInicio' é obrigatório!")
        @PastOrPresent(message = "O campo 'dataInicio' não pode estar no futuro!")
        LocalDate dataInicio,

        @NotNull(message = "O campo 'reservaEmergencia' é obrigatório!")
        Boolean reservaEmergencia,

        UUID idConta,

        @Size(max = 500, message = "O campo 'observacoes' deve ter no máximo 500 caracteres!")
        String observacoes

) {}