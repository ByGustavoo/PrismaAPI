package br.com.prismaapi.model.mapper.despesarecorrente;

import br.com.prismaapi.model.dto.despesarecorrente.DespesaRecorrenteDTO;
import br.com.prismaapi.model.dto.despesarecorrente.SalvarDespesaRecorrenteDTO;
import br.com.prismaapi.model.entity.despesarecorrente.DespesaRecorrente;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = CategoriaMapper.class)
public interface DespesaRecorrenteMapper {

    @Mapping(target = "idOrigem", expression = "java(idOrigem(despesaRecorrente))")
    @Mapping(target = "nomeOrigem", expression = "java(nomeOrigem(despesaRecorrente))")
    DespesaRecorrenteDTO toDTO(DespesaRecorrente despesaRecorrente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    DespesaRecorrente toEntity(DespesaRecorrenteDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    DespesaRecorrente toEntity(SalvarDespesaRecorrenteDTO salvarDespesaRecorrenteDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    void updateEntity(SalvarDespesaRecorrenteDTO salvarDespesaRecorrenteDTO, @MappingTarget DespesaRecorrente despesaRecorrente);

    default UUID idOrigem(DespesaRecorrente despesaRecorrente) {
        if (despesaRecorrente.getConta() != null) return despesaRecorrente.getConta().getId();
        return despesaRecorrente.getCartao() != null ? despesaRecorrente.getCartao().getId() : null;
    }

    default String nomeOrigem(DespesaRecorrente despesaRecorrente) {
        if (despesaRecorrente.getConta() != null) return despesaRecorrente.getConta().getNome();
        return despesaRecorrente.getCartao() != null ? despesaRecorrente.getCartao().getNome() : null;
    }
}