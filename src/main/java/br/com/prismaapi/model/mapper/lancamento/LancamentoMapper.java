package br.com.prismaapi.model.mapper.lancamento;

import br.com.prismaapi.model.dto.lancamento.LancamentoDTO;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = CategoriaMapper.class)
public interface LancamentoMapper {

    @Mapping(target = "idOrigem", expression = "java(idOrigem(lancamento))")
    @Mapping(target = "nomeOrigem", expression = "java(nomeOrigem(lancamento))")
    @Mapping(target = "idContaDestino", source = "contaDestino.id")
    @Mapping(target = "nomeContaDestino", source = "contaDestino.nome")
    LancamentoDTO toDTO(Lancamento lancamento);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "contaDestino", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Lancamento toEntity(LancamentoDTO dto);

    default UUID idOrigem(Lancamento lancamento) {
        if (lancamento.getConta() != null) return lancamento.getConta().getId();
        return lancamento.getCartao() != null ? lancamento.getCartao().getId() : null;
    }

    default String nomeOrigem(Lancamento lancamento) {
        if (lancamento.getConta() != null) return lancamento.getConta().getNome();
        return lancamento.getCartao() != null ? lancamento.getCartao().getNome() : null;
    }
}