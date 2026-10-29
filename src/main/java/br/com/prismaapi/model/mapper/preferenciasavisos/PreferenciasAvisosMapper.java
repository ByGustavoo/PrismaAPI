package br.com.prismaapi.model.mapper.preferenciasavisos;

import br.com.prismaapi.model.dto.aviso.PreferenciasAvisosDTO;
import br.com.prismaapi.model.entity.preferenciasavisos.PreferenciasAvisos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PreferenciasAvisosMapper {

    PreferenciasAvisosDTO toDTO(PreferenciasAvisos preferenciasAvisos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    void updateEntity(PreferenciasAvisosDTO preferenciasAvisosDTO, @MappingTarget PreferenciasAvisos preferenciasAvisos);
}