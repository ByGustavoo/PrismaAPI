package br.com.prismaapi.model.dto.conta;

import br.com.prismaapi.enums.TipoMovimentacaoConta;
import br.com.prismaapi.model.entity.lancamento.Lancamento;

public record EfeitoNaConta(

        Lancamento lancamento,

        TipoMovimentacaoConta tipo

) {}