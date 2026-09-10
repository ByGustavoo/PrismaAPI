package br.com.prismaapi.model.mapper.cartao;

import br.com.prismaapi.model.dto.cartao.CartaoDTO;
import br.com.prismaapi.model.dto.cartao.SalvarCartaoDTO;
import br.com.prismaapi.model.dto.conta.OrigemDTO;
import br.com.prismaapi.model.entity.cartao.Cartao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartaoMapper {

    @Mapping(target = "idConta", source = "cartao.conta.id")
    @Mapping(target = "nomeConta", source = "cartao.conta.nome")
    @Mapping(target = "limiteComprometido", source = "limiteComprometido")
    CartaoDTO toDTO(Cartao cartao, BigDecimal limiteComprometido);

    @Mapping(target = "grupo", constant = "CARTAO")
    OrigemDTO toOrigemDTO(Cartao cartao);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Cartao toEntity(CartaoDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Cartao toEntity(SalvarCartaoDTO salvarCartaoDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    void updateEntity(SalvarCartaoDTO salvarCartaoDTO, @MappingTarget Cartao cartao);
}