package br.com.prismaapi.model.dto.conta;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoConta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Representa o modelo de requisição para salvar uma Conta.")
public record SalvarContaDTO(

        @NotBlank(message = "O campo 'nome' é obrigatório!")
        @Size(min = 2, max = 80, message = "O campo 'nome' deve ter entre 2 e 80 caracteres!")
        String nome,

        @NotBlank(message = "O campo 'instituicao' é obrigatório!")
        @Size(min = 2, max = 80, message = "O campo 'instituicao' deve ter entre 2 e 80 caracteres!")
        String instituicao,

        @NotNull(message = "O campo 'tipo' é obrigatório!")
        TipoConta tipo,

        @NotNull(message = "O campo 'saldo' é obrigatório!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'saldo' deve ter no máximo duas casas decimais!")
        BigDecimal saldo,

        @NotNull(message = "O campo 'situacao' é obrigatório!")
        Situacao situacao,

        @NotNull(message = "O campo 'incluirNoTotal' é obrigatório!")
        Boolean incluirNoTotal

) {}