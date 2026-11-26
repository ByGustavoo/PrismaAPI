package br.com.prismaapi.model.dto.lancamento;

import br.com.prismaapi.enums.DirecaoOrdenacao;
import br.com.prismaapi.enums.OrdenacaoLancamento;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoLancamento;

import java.time.LocalDate;
import java.util.UUID;

public record FiltroLancamentoDTO(

        TipoLancamento tipo,

        String busca,

        LocalDate dataInicial,

        LocalDate dataFinal,

        UUID idCategoria,

        UUID idOrigem,

        SituacaoLancamento situacao,

        int pagina,

        int tamanho,

        OrdenacaoLancamento ordenacao,

        DirecaoOrdenacao direcao

) {}