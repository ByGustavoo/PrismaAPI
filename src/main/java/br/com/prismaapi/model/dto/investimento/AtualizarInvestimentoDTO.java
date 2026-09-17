package br.com.prismaapi.model.dto.investimento;

import br.com.prismaapi.enums.ClasseAtivo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Representa o modelo de requisição para atualizar o cadastro de um Investimento, sem os valores.")
public record AtualizarInvestimentoDTO(

        @NotBlank(message = "O campo 'nome' é obrigatório!")
        @Size(min = 2, max = 120, message = "O campo 'nome' deve ter entre 2 e 120 caracteres!")
        String nome,

        @NotNull(message = "O campo 'classeAtivo' é obrigatório!")
        ClasseAtivo classeAtivo,

        @NotBlank(message = "O campo 'instituicao' é obrigatório!")
        @Size(min = 2, max = 80, message = "O campo 'instituicao' deve ter entre 2 e 80 caracteres!")
        String instituicao,

        @Size(max = 500, message = "O campo 'observacoes' deve ter no máximo 500 caracteres!")
        String observacoes

) {}