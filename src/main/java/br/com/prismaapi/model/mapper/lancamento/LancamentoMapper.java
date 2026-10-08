package br.com.prismaapi.model.mapper.lancamento;

import br.com.prismaapi.model.dto.lancamento.LancamentoDTO;
import br.com.prismaapi.model.dto.lancamento.ParcelaLancamentoDTO;
import br.com.prismaapi.model.dto.lancamento.SalvarLancamentoDTO;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = CategoriaMapper.class)
public interface LancamentoMapper {

    @Mapping(target = "idContaDestino", source = "contaDestino.id")
    @Mapping(target = "nomeContaDestino", source = "contaDestino.nome")
    @Mapping(target = "parcela", expression = "java(parcela(lancamento))")
    @Mapping(target = "idOrigem", expression = "java(idOrigem(lancamento))")
    @Mapping(target = "nomeOrigem", expression = "java(nomeOrigem(lancamento))")
    LancamentoDTO toDTO(Lancamento lancamento);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "parcelaPaga", ignore = true)
    @Mapping(target = "contaDestino", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "dataPagamentoFatura", ignore = true)
    @Mapping(target = "contaPagamentoFatura", ignore = true)
    Lancamento toEntity(SalvarLancamentoDTO salvarLancamentoDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conta", ignore = true)
    @Mapping(target = "cartao", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "parcelaPaga", ignore = true)
    @Mapping(target = "contaDestino", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "dataPagamentoFatura", ignore = true)
    @Mapping(target = "contaPagamentoFatura", ignore = true)
    void updateEntity(SalvarLancamentoDTO salvarLancamentoDTO, @MappingTarget Lancamento lancamento);

    default UUID idOrigem(Lancamento lancamento) {
        if (lancamento.getConta() != null) return lancamento.getConta().getId();
        return lancamento.getCartao() != null ? lancamento.getCartao().getId() : null;
    }

    default String nomeOrigem(Lancamento lancamento) {
        if (lancamento.getConta() != null) return lancamento.getConta().getNome();
        return lancamento.getCartao() != null ? lancamento.getCartao().getNome() : null;
    }

    default ParcelaLancamentoDTO parcela(Lancamento lancamento) {
        var pagamento = lancamento.getParcelaPaga();

        if (pagamento == null) return null;

        var compra = pagamento.getCompraParcelada();

        return new ParcelaLancamentoDTO(compra.getId(), pagamento.getNumero().intValue(), compra.getParcelas().intValue());
    }
}