package br.com.prismaapi.model.mapper.conta;

import br.com.prismaapi.model.dto.conta.ContaDTO;
import br.com.prismaapi.model.entity.conta.Conta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContaMapper {

    ContaDTO toDTO(Conta conta);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Conta toEntity(ContaDTO dto);

}
