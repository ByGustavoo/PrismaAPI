package br.com.prismaapi.model.mapper.fatura;

import br.com.prismaapi.model.dto.fatura.DetalheFaturaDTO;
import br.com.prismaapi.model.dto.fatura.FaturaCartaoDTO;
import br.com.prismaapi.model.dto.fatura.ItemFaturaDTO;
import br.com.prismaapi.model.dto.fatura.ParcelaItemFaturaDTO;
import br.com.prismaapi.model.entity.compraparcelada.CompraParcelada;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", uses = CategoriaMapper.class)
public interface FaturaMapper {

    @Mapping(target = "itens", source = "itens")
    DetalheFaturaDTO toDetalheDTO(FaturaCartaoDTO fatura, List<ItemFaturaDTO> itens);

    @Mapping(target = "parcela", ignore = true)
    ItemFaturaDTO toItemDTO(Lancamento lancamento);

    @Mapping(target = "id", expression = "java(compraParcelada.getId() + \"-\" + parcela.numero())")
    @Mapping(target = "descricao", source = "compraParcelada.descricao")
    @Mapping(target = "data", source = "compraParcelada.dataCompra")
    @Mapping(target = "valor", source = "valor")
    @Mapping(target = "categoria", source = "compraParcelada.categoria")
    @Mapping(target = "parcela", source = "parcela")
    ItemFaturaDTO toItemParceladoDTO(CompraParcelada compraParcelada, ParcelaItemFaturaDTO parcela, BigDecimal valor);
}