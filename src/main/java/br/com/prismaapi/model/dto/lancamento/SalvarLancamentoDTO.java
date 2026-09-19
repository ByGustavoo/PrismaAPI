package br.com.prismaapi.model.dto.lancamento;

import br.com.prismaapi.enums.FormaLancamento;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoLancamento;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Representa o modelo de requisição para salvar um Lançamento.")
public record SalvarLancamentoDTO(

        @NotBlank(message = "O campo 'descricao' é obrigatório!")
        @Size(min = 2, max = 160, message = "O campo 'descricao' deve ter entre 2 e 160 caracteres!")
        String descricao,

        @NotNull(message = "O campo 'valor' é obrigatório!")
        @Positive(message = "O campo 'valor' deve ser maior que zero!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'valor' deve ter no máximo duas casas decimais!")
        BigDecimal valor,

        @NotNull(message = "O campo 'tipo' é obrigatório!")
        TipoLancamento tipo,

        @NotNull(message = "O campo 'situacao' é obrigatório!")
        SituacaoLancamento situacao,

        @NotNull(message = "O campo 'forma' é obrigatório!")
        FormaLancamento forma,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "O campo 'data' é obrigatório!")
        LocalDate data,

        UUID idCategoria,

        @NotNull(message = "O campo 'idOrigem' é obrigatório!")
        UUID idOrigem,

        UUID idContaDestino,

        @Size(max = 500, message = "O campo 'observacoes' deve ter no máximo 500 caracteres!")
        String observacoes

) {}