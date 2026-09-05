package br.com.prismaapi.model.mapper.categoria;

import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import br.com.prismaapi.model.entity.categoria.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    CategoriaDTO toDTO(Categoria categoria);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Categoria toEntity(CategoriaDTO dto);

}
