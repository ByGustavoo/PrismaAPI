package br.com.prismaapi.model.dto.categoria;

import br.com.prismaapi.enums.TipoCategoria;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de uma Categoria.")
public record CategoriaDTO(

        UUID id,

        String nome,

        TipoCategoria tipo,

        Short tokenCor,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataCriacao,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataAtualizacao

) {}
