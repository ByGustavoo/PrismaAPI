package br.com.prismaapi.model.dto.investimento;

import br.com.prismaapi.enums.ClasseAtivo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Representa o modelo de requisição para atualizar o cadastro de um Investimento, sem os valores. A quantidade só vale para ações ainda sem ela.")
public record AtualizarInvestimentoDTO(

        @NotBlank(message = "O campo 'nome' é obrigatório!")
        @Size(min = 2, max = 120, message = "O campo 'nome' deve ter entre 2 e 120 caracteres!")
        String nome,

        @Pattern(regexp = "^[A-Za-z0-9]{4,12}$", message = "O campo 'codigo' deve ter de 4 a 12 letras e números, como PETR4!")
        String codigo,

        @NotNull(message = "O campo 'classeAtivo' é obrigatório!")
        ClasseAtivo classeAtivo,

        @NotBlank(message = "O campo 'instituicao' é obrigatório!")
        @Size(min = 2, max = 80, message = "O campo 'instituicao' deve ter entre 2 e 80 caracteres!")
        String instituicao,

        @NotNull(message = "O campo 'reservaEmergencia' é obrigatório!")
        Boolean reservaEmergencia,

        @Positive(message = "O campo 'quantidade' deve ser maior que zero!")
        @Digits(integer = 10, fraction = 0, message = "O campo 'quantidade' deve ser um número inteiro de ações!")
        BigDecimal quantidade,

        @Size(max = 500, message = "O campo 'observacoes' deve ter no máximo 500 caracteres!")
        String observacoes

) {}