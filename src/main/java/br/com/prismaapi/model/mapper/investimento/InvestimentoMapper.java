package br.com.prismaapi.model.mapper.investimento;

import br.com.prismaapi.model.dto.investimento.AtualizarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.InvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarAcaoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarInvestimentoDTO;
import br.com.prismaapi.model.entity.investimento.Investimento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface InvestimentoMapper {

    @Mapping(target = "precoMedio", source = "precoMedio")
    @Mapping(target = "dataAtualizacao", source = "investimento.dataUltimaMovimentacao")
    InvestimentoDTO toDTO(Investimento investimento, BigDecimal precoMedio);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "cotacao", ignore = true)
    @Mapping(target = "quantidade", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "dataUltimaMovimentacao", ignore = true)
    Investimento toEntity(SalvarInvestimentoDTO salvarInvestimentoDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cotacao", ignore = true)
    @Mapping(target = "aportado", ignore = true)
    @Mapping(target = "quantidade", ignore = true)
    @Mapping(target = "valorAtual", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "classeAtivo", constant = "ACOES")
    @Mapping(target = "dataUltimaMovimentacao", ignore = true)
    Investimento toEntity(SalvarAcaoDTO salvarAcaoDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "cotacao", ignore = true)
    @Mapping(target = "aportado", ignore = true)
    @Mapping(target = "quantidade", ignore = true)
    @Mapping(target = "valorAtual", ignore = true)
    @Mapping(target = "dataInicio", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "dataUltimaMovimentacao", ignore = true)
    void updateEntity(AtualizarInvestimentoDTO atualizarInvestimentoDTO, @MappingTarget Investimento investimento);
}