package br.com.prismaapi.model.dto.categoria;

import br.com.prismaapi.enums.TipoCategoria;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Representa o modelo de dados de uma Categoria.")
public record CategoriaDTO(

        UUID id,

        String nome,

        TipoCategoria tipo,

        Short tokenCor

) {}