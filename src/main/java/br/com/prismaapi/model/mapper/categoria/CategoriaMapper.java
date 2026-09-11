package br.com.prismaapi.model.mapper.categoria;

import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import br.com.prismaapi.model.entity.categoria.Categoria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    CategoriaDTO toDTO(Categoria categoria);

}
