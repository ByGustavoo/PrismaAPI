package br.com.prismaapi.model.dto.relatorio;

import br.com.prismaapi.model.dto.dashboard.fluxo.FluxoDTO;
import br.com.prismaapi.model.dto.dashboard.gasto.categoria.GastoCategoriaDTO;
import br.com.prismaapi.model.dto.dashboard.saldo.SaldoDTO;
import br.com.prismaapi.model.dto.dashboard.variacao.VariacaoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Representa o relatório consolidado de um período.")
public record RelatorioDTO(

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataInicial,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataFinal,

        BigDecimal receitas,

        BigDecimal despesas,

        BigDecimal resultado,

        VariacaoDTO variacaoReceitas,

        VariacaoDTO variacaoDespesas,

        Integer quantidadeLancamentos,

        List<GastoCategoriaDTO> despesasPorCategoria,

        List<GastoCategoriaDTO> receitasPorCategoria,

        List<FluxoDTO> fluxoCaixa,

        List<GastoOrigemDTO> despesasPorOrigem,

        List<SaldoDTO> historicoSaldo,

        List<PatrimonioDTO> patrimonio

) {}