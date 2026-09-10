package br.com.prismaapi.model.dto.cartao;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoCartao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Representa o modelo de requisição para salvar um Cartão.")
public record SalvarCartaoDTO(

        @NotBlank(message = "O campo 'nome' é obrigatório!")
        @Size(min = 2, max = 80, message = "O campo 'nome' deve ter entre 2 e 80 caracteres!")
        String nome,

        @NotBlank(message = "O campo 'instituicao' é obrigatório!")
        @Size(min = 2, max = 80, message = "O campo 'instituicao' deve ter entre 2 e 80 caracteres!")
        String instituicao,

        @NotNull(message = "O campo 'tipo' é obrigatório!")
        TipoCartao tipo,

        @NotNull(message = "O campo 'situacao' é obrigatório!")
        Situacao situacao,

        @Size(max = 40, message = "O campo 'bandeira' deve ter no máximo 40 caracteres!")
        String bandeira,

        @Pattern(regexp = "^[0-9]{4}$", message = "O campo 'ultimosDigitos' deve ter exatamente 4 números!")
        String ultimosDigitos,

        @Positive(message = "O campo 'limiteCredito' deve ser maior que zero!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'limiteCredito' deve ter no máximo duas casas decimais!")
        BigDecimal limiteCredito,

        @Min(value = 1, message = "O campo 'diaFechamento' deve estar entre 1 e 31!")
        @Max(value = 31, message = "O campo 'diaFechamento' deve estar entre 1 e 31!")
        Short diaFechamento,

        @Min(value = 1, message = "O campo 'diaVencimento' deve estar entre 1 e 31!")
        @Max(value = 31, message = "O campo 'diaVencimento' deve estar entre 1 e 31!")
        Short diaVencimento,

        UUID idConta,

        @PositiveOrZero(message = "O campo 'saldo' não pode ser negativo!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'saldo' deve ter no máximo duas casas decimais!")
        BigDecimal saldo

) {}