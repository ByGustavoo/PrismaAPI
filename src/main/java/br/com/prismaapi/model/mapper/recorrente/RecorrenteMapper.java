package br.com.prismaapi.model.mapper.recorrente;

import br.com.prismaapi.model.dto.recorrente.RecorrenteDTO;
import br.com.prismaapi.model.dto.recorrente.SalvarRecorrenteDTO;
import br.com.prismaapi.model.entity.recorrente.Recorrente;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = CategoriaMapper.class)
public interface RecorrenteMapper {

    @Mapping(target = "idOrigem", expression = "java(idOrigem(recorrente))")
    @Mapping(target = "nomeOrigem", expression = "java(nomeOrigem(recorrente))")
    RecorrenteDTO toDTO(Recorrente recorrente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Recorrente toEntity(SalvarRecorrenteDTO salvarRecorrenteDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    void updateEntity(SalvarRecorrenteDTO salvarRecorrenteDTO, @MappingTarget Recorrente recorrente);

    default UUID idOrigem(Recorrente recorrente) {
        if (recorrente.getConta() != null) return recorrente.getConta().getId();
        return recorrente.getCartao() != null ? recorrente.getCartao().getId() : null;
    }

    default String nomeOrigem(Recorrente recorrente) {
        if (recorrente.getConta() != null) return recorrente.getConta().getNome();
        return recorrente.getCartao() != null ? recorrente.getCartao().getNome() : null;
    }
}