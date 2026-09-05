package br.com.prismaapi.model.mapper.compraparcelada;

import br.com.prismaapi.model.dto.compraparcelada.CompraParceladaDTO;
import br.com.prismaapi.model.entity.compraparcelada.CompraParcelada;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompraParceladaMapper {

    @Mapping(target = "cartaoId", source = "cartao.id")
    @Mapping(target = "categoriaId", source = "categoria.id")
    CompraParceladaDTO toDTO(CompraParcelada compraParcelada);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    CompraParcelada toEntity(CompraParceladaDTO dto);

}
