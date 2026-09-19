package br.com.prismaapi.model.mapper.movimentacaoinvestimento;

import br.com.prismaapi.model.dto.investimento.MovimentacaoInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarAporteInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarSaldoInvestimentoDTO;
import br.com.prismaapi.model.entity.movimentacaoinvestimento.MovimentacaoInvestimento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface MovimentacaoInvestimentoMapper {

    @Mapping(target = "id", source = "movimentacao.id")
    @Mapping(target = "valor", source = "valorExibido")
    @Mapping(target = "saldoApos", source = "saldoApos")
    @Mapping(target = "tipo", source = "movimentacao.tipo")
    @Mapping(target = "data", source = "movimentacao.data")
    @Mapping(target = "aportadoApos", source = "aportadoApos")
    @Mapping(target = "descricao", source = "movimentacao.descricao")
    MovimentacaoInvestimentoDTO toDTO(MovimentacaoInvestimento movimentacao, BigDecimal valorExibido, BigDecimal saldoApos, BigDecimal aportadoApos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipo", constant = "APORTE")
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "investimento", ignore = true)
    @Mapping(target = "saldoInformado", ignore = true)
    MovimentacaoInvestimento toEntity(SalvarAporteInvestimentoDTO salvarAporteInvestimentoDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "valor", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "investimento", ignore = true)
    @Mapping(target = "tipo", constant = "RENDIMENTO")
    @Mapping(target = "saldoInformado", source = "valorAtual")
    MovimentacaoInvestimento toEntity(SalvarSaldoInvestimentoDTO salvarSaldoInvestimentoDTO);
}