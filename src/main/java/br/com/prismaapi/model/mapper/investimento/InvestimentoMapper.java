package br.com.prismaapi.model.mapper.investimento;

import br.com.prismaapi.model.dto.investimento.AtualizarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.InvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarInvestimentoDTO;
import br.com.prismaapi.model.entity.investimento.Investimento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InvestimentoMapper {

    @Mapping(target = "dataAtualizacao", source = "dataUltimaMovimentacao")
    InvestimentoDTO toDTO(Investimento investimento);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "dataUltimaMovimentacao", ignore = true)
    Investimento toEntity(SalvarInvestimentoDTO salvarInvestimentoDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aportado", ignore = true)
    @Mapping(target = "valorAtual", ignore = true)
    @Mapping(target = "dataInicio", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "dataUltimaMovimentacao", ignore = true)
    void updateEntity(AtualizarInvestimentoDTO atualizarInvestimentoDTO, @MappingTarget Investimento investimento);
}