package br.com.prismaapi.model.mapper.orcamento;

import br.com.prismaapi.model.dto.orcamento.OrcamentoDTO;
import br.com.prismaapi.model.dto.orcamento.SalvarOrcamentoDTO;
import br.com.prismaapi.model.entity.orcamento.Orcamento;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = CategoriaMapper.class)
public interface OrcamentoMapper {

    OrcamentoDTO toDTO(Orcamento orcamento);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Orcamento toEntity(OrcamentoDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Orcamento toEntity(SalvarOrcamentoDTO salvarOrcamentoDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    void updateEntity(SalvarOrcamentoDTO salvarOrcamentoDTO, @MappingTarget Orcamento orcamento);
}