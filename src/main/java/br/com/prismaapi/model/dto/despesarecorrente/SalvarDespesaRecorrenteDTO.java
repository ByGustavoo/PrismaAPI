package br.com.prismaapi.model.dto.despesarecorrente;

import br.com.prismaapi.enums.Frequencia;
import br.com.prismaapi.enums.SituacaoDespesaRecorrente;
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

@Schema(description = "Representa o modelo de requisição para salvar uma Despesa Recorrente.")
public record SalvarDespesaRecorrenteDTO(

        @NotBlank(message = "O campo 'descricao' é obrigatório!")
        @Size(min = 2, max = 160, message = "O campo 'descricao' deve ter entre 2 e 160 caracteres!")
        String descricao,

        @NotNull(message = "O campo 'valor' é obrigatório!")
        @Positive(message = "O campo 'valor' deve ser maior que zero!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'valor' deve ter no máximo duas casas decimais!")
        BigDecimal valor,

        UUID idCategoria,

        @NotNull(message = "O campo 'frequencia' é obrigatório!")
        Frequencia frequencia,

        @NotNull(message = "O campo 'proximoVencimento' é obrigatório!")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate proximoVencimento,

        @NotNull(message = "O campo 'idOrigem' é obrigatório!")
        UUID idOrigem,

        @NotNull(message = "O campo 'situacao' é obrigatório!")
        SituacaoDespesaRecorrente situacao,

        @Size(max = 500, message = "O campo 'observacoes' deve ter no máximo 500 caracteres!")
        String observacoes

) {}