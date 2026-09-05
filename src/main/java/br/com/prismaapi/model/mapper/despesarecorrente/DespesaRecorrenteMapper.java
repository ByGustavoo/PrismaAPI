package br.com.prismaapi.model.mapper.despesarecorrente;

import br.com.prismaapi.model.dto.despesarecorrente.DespesaRecorrenteDTO;
import br.com.prismaapi.model.entity.despesarecorrente.DespesaRecorrente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DespesaRecorrenteMapper {

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "contaId", source = "conta.id")
    @Mapping(target = "cartaoId", source = "cartao.id")
    DespesaRecorrenteDTO toDTO(DespesaRecorrente despesaRecorrente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    DespesaRecorrente toEntity(DespesaRecorrenteDTO dto);

}
