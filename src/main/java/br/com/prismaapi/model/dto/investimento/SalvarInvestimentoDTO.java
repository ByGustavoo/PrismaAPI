package br.com.prismaapi.model.dto.investimento;

import br.com.prismaapi.enums.ClasseAtivo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Representa o modelo de requisição para salvar um Investimento.")
public record SalvarInvestimentoDTO(

        @NotBlank(message = "O campo 'nome' é obrigatório!")
        @Size(min = 2, max = 120, message = "O campo 'nome' deve ter entre 2 e 120 caracteres!")
        String nome,

        @NotNull(message = "O campo 'classeAtivo' é obrigatório!")
        ClasseAtivo classeAtivo,

        @NotBlank(message = "O campo 'instituicao' é obrigatório!")
        @Size(min = 2, max = 80, message = "O campo 'instituicao' deve ter entre 2 e 80 caracteres!")
        String instituicao,

        @NotNull(message = "O campo 'aportado' é obrigatório!")
        @Positive(message = "O campo 'aportado' deve ser maior que zero!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'aportado' deve ter no máximo duas casas decimais!")
        BigDecimal aportado,

        @NotNull(message = "O campo 'valorAtual' é obrigatório!")
        @PositiveOrZero(message = "O campo 'valorAtual' não pode ser negativo!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'valorAtual' deve ter no máximo duas casas decimais!")
        BigDecimal valorAtual,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "O campo 'dataInicio' é obrigatório!")
        @PastOrPresent(message = "O campo 'dataInicio' não pode estar no futuro!")
        LocalDate dataInicio,

        @Size(max = 500, message = "O campo 'observacoes' deve ter no máximo 500 caracteres!")
        String observacoes

) {}