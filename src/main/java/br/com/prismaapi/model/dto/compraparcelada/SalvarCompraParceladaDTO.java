package br.com.prismaapi.model.dto.compraparcelada;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Schema(description = "Representa o modelo de requisição para salvar uma Compra Parcelada.")
public record SalvarCompraParceladaDTO(

        @NotBlank(message = "O campo 'descricao' é obrigatório!")
        @Size(min = 2, max = 160, message = "O campo 'descricao' deve ter entre 2 e 160 caracteres!")
        String descricao,

        @NotNull(message = "O campo 'valorTotal' é obrigatório!")
        @Positive(message = "O campo 'valorTotal' deve ser maior que zero!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'valorTotal' deve ter no máximo duas casas decimais!")
        BigDecimal valorTotal,

        @NotNull(message = "O campo 'parcelas' é obrigatório!")
        @Min(value = 2, message = "O campo 'parcelas' deve estar entre 2 e 48!")
        @Max(value = 48, message = "O campo 'parcelas' deve estar entre 2 e 48!")
        Short parcelas,

        @NotNull(message = "O campo 'dataCompra' é obrigatório!")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataCompra,

        @NotNull(message = "O campo 'primeiroMes' é obrigatório!")
        @JsonFormat(pattern = "yyyy-MM")
        YearMonth primeiroMes,

        @NotNull(message = "O campo 'idCartao' é obrigatório!")
        UUID idCartao,

        UUID idCategoria,

        @Size(max = 500, message = "O campo 'observacoes' deve ter no máximo 500 caracteres!")
        String observacoes

) {}