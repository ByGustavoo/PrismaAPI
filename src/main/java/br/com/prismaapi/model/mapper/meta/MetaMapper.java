package br.com.prismaapi.model.mapper.meta;

import br.com.prismaapi.model.dto.meta.MetaDTO;
import br.com.prismaapi.model.entity.meta.Meta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MetaMapper {

    MetaDTO toDTO(Meta meta);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Meta toEntity(MetaDTO dto);

}
