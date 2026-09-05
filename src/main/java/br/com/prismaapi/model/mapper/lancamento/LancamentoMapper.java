package br.com.prismaapi.model.mapper.lancamento;

import br.com.prismaapi.model.dto.lancamento.LancamentoDTO;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LancamentoMapper {

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "contaId", source = "conta.id")
    @Mapping(target = "cartaoId", source = "cartao.id")
    @Mapping(target = "contaDestinoId", source = "contaDestino.id")
    LancamentoDTO toDTO(Lancamento lancamento);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "contaDestino", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Lancamento toEntity(LancamentoDTO dto);

}
