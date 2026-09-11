package br.com.prismaapi.model.mapper.meta;

import br.com.prismaapi.model.dto.meta.AtualizarMetaDTO;
import br.com.prismaapi.model.dto.meta.MetaDTO;
import br.com.prismaapi.model.dto.meta.SalvarMetaDTO;
import br.com.prismaapi.model.entity.meta.Meta;
import br.com.prismaapi.model.entity.metapreco.MetaPreco;
import br.com.prismaapi.model.mapper.metapreco.MetaPrecoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring", uses = MetaPrecoMapper.class)
public interface MetaMapper {

    @Mapping(target = "id", source = "meta.id")
    @Mapping(target = "historico", source = "historico")
    @Mapping(target = "dataCriacao", source = "dataCriacao")
    MetaDTO toDTO(Meta meta, LocalDate dataCriacao, List<MetaPreco> historico);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Meta toEntity(SalvarMetaDTO salvarMetaDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    void updateEntity(AtualizarMetaDTO atualizarMetaDTO, @MappingTarget Meta meta);
}