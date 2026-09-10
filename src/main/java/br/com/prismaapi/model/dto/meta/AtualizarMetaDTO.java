package br.com.prismaapi.model.dto.meta;

import br.com.prismaapi.enums.SituacaoMeta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Representa o modelo de requisição para atualizar uma Meta, sem mexer no preço.")
public record AtualizarMetaDTO(

        @NotBlank(message = "O campo 'nome' é obrigatório!")
        @Size(min = 2, max = 120, message = "O campo 'nome' deve ter entre 2 e 120 caracteres!")
        String nome,

        @Size(max = 2048, message = "O campo 'url' deve ter no máximo 2048 caracteres!")
        @Pattern(regexp = "^$|^https?://\\S+$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "O campo 'url' deve começar com http:// ou https://!")
        String url,

        @Size(max = 2048, message = "O campo 'urlImagem' deve ter no máximo 2048 caracteres!")
        @Pattern(regexp = "^$|^https?://\\S+$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "O campo 'urlImagem' deve começar com http:// ou https://!")
        String urlImagem,

        @NotNull(message = "O campo 'situacao' é obrigatório!")
        SituacaoMeta situacao,

        @Size(max = 500, message = "O campo 'observacoes' deve ter no máximo 500 caracteres!")
        String observacoes

) {}