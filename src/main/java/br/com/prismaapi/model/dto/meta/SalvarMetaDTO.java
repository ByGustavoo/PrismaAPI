package br.com.prismaapi.model.dto.meta;

import br.com.prismaapi.enums.SituacaoMeta;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Representa o modelo de requisição para cadastrar uma Meta com o primeiro preço.")
public record SalvarMetaDTO(

        @NotBlank(message = "O campo 'nome' é obrigatório!")
        @Size(min = 2, max = 120, message = "O campo 'nome' deve ter entre 2 e 120 caracteres!")
        String nome,

        @Size(max = 2048, message = "O campo 'url' deve ter no máximo 2048 caracteres!")
        @Pattern(regexp = "^$|^https?://\\S+$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "O campo 'url' deve começar com http:// ou https://!")
        String url,

        @Size(max = 2048, message = "O campo 'urlImagem' deve ter no máximo 2048 caracteres!")
        @Pattern(regexp = "^$|^https?://\\S+$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "O campo 'urlImagem' deve começar com http:// ou https://!")
        String urlImagem,

        @NotNull(message = "O campo 'preco' é obrigatório!")
        @Positive(message = "O campo 'preco' deve ser maior que zero!")
        @Digits(integer = 12, fraction = 2, message = "O campo 'preco' deve ter no máximo duas casas decimais!")
        BigDecimal preco,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "O campo 'data' é obrigatório!")
        @PastOrPresent(message = "O campo 'data' não pode estar no futuro!")
        LocalDate data,

        @NotNull(message = "O campo 'situacao' é obrigatório!")
        SituacaoMeta situacao,

        @Size(max = 500, message = "O campo 'observacoes' deve ter no máximo 500 caracteres!")
        String observacoes

) {}