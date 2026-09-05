package br.com.prismaapi.model.mapper.cartao;

import br.com.prismaapi.model.dto.cartao.CartaoDTO;
import br.com.prismaapi.model.entity.cartao.Cartao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartaoMapper {

    @Mapping(target = "contaId", source = "conta.id")
    CartaoDTO toDTO(Cartao cartao);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Cartao toEntity(CartaoDTO dto);

}
