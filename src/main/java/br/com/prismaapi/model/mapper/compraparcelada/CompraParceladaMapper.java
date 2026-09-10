package br.com.prismaapi.model.mapper.compraparcelada;

import br.com.prismaapi.model.dto.compraparcelada.CompraParceladaDTO;
import br.com.prismaapi.model.dto.compraparcelada.SalvarCompraParceladaDTO;
import br.com.prismaapi.model.entity.compraparcelada.CompraParcelada;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = CategoriaMapper.class)
public interface CompraParceladaMapper {

    @Mapping(target = "idCartao", source = "cartao.id")
    @Mapping(target = "nomeCartao", source = "cartao.nome")
    CompraParceladaDTO toDTO(CompraParcelada compraParcelada);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    CompraParcelada toEntity(CompraParceladaDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "primeiroMes", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    CompraParcelada toEntity(SalvarCompraParceladaDTO salvarCompraParceladaDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "primeiroMes", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    void updateEntity(SalvarCompraParceladaDTO salvarCompraParceladaDTO, @MappingTarget CompraParcelada compraParcelada);
}