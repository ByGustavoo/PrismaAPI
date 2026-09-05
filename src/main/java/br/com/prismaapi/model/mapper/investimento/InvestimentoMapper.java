package br.com.prismaapi.model.mapper.investimento;

import br.com.prismaapi.model.dto.investimento.InvestimentoDTO;
import br.com.prismaapi.model.entity.investimento.Investimento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvestimentoMapper {

    InvestimentoDTO toDTO(Investimento investimento);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Investimento toEntity(InvestimentoDTO dto);

}
