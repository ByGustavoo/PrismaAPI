package br.com.prismaapi.model.mapper.orcamento;

import br.com.prismaapi.model.dto.orcamento.OrcamentoDTO;
import br.com.prismaapi.model.entity.orcamento.Orcamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrcamentoMapper {

    @Mapping(target = "categoriaId", source = "categoria.id")
    OrcamentoDTO toDTO(Orcamento orcamento);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Orcamento toEntity(OrcamentoDTO dto);

}
