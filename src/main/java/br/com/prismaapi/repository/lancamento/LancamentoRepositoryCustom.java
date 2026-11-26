package br.com.prismaapi.repository.lancamento;

import br.com.prismaapi.model.entity.lancamento.Lancamento;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public interface LancamentoRepositoryCustom {

    BigDecimal somarResultado(Specification<Lancamento> especificacao);
}