package br.com.prismaapi.model.dto.lancamento;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Representa uma página de lançamentos e os totais do filtro inteiro.")
public record PaginaLancamentosDTO(

        @Schema(description = "Lançamentos da página, na ordem pedida")
        List<LancamentoDTO> itens,

        @Schema(description = "Número da página, começando em zero", example = "0")
        int pagina,

        @Schema(description = "Quantidade máxima de lançamentos por página", example = "25")
        int tamanho,

        @Schema(description = "Quantidade de lançamentos que passam no filtro, somando todas as páginas", example = "279")
        long quantidadeTotal,

        @Schema(description = "Quantidade de páginas do filtro", example = "12")
        int quantidadePaginas,

        @Schema(description = "Receitas menos despesas de todos os lançamentos do filtro; transferências ficam de fora", example = "19648.53")
        BigDecimal resultado

) {}