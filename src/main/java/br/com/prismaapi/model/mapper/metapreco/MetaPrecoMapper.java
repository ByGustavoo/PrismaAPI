package br.com.prismaapi.model.mapper.metapreco;

import br.com.prismaapi.model.dto.metapreco.MetaPrecoDTO;
import br.com.prismaapi.model.dto.metapreco.SalvarMetaPrecoDTO;
import br.com.prismaapi.model.entity.metapreco.MetaPreco;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MetaPrecoMapper {

    MetaPrecoDTO toDTO(MetaPreco metaPreco);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "meta", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    MetaPreco toEntity(MetaPrecoDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "meta", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    MetaPreco toEntity(SalvarMetaPrecoDTO salvarMetaPrecoDTO);
}