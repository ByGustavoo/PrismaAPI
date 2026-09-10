package br.com.prismaapi.model.dto.dashboard;

import br.com.prismaapi.model.dto.dashboard.fatura.FaturaDTO;
import br.com.prismaapi.model.dto.dashboard.fluxo.FluxoDTO;
import br.com.prismaapi.model.dto.dashboard.gasto.categoria.GastoCategoriaDTO;
import br.com.prismaapi.model.dto.dashboard.gasto.diario.GastoDiarioDTO;
import br.com.prismaapi.model.dto.lancamento.LancamentoDTO;
import br.com.prismaapi.model.dto.dashboard.saldo.SaldoDTO;
import br.com.prismaapi.model.dto.dashboard.variacao.VariacaoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDTO(

        @JsonFormat(pattern = "yyyy-MM")
        String de,

        @JsonFormat(pattern = "yyyy-MM")
        String ate,

        BigDecimal saldoAtual,

        VariacaoDTO variacaoSaldo,

        BigDecimal receitasMes,

        VariacaoDTO variacaoReceitas,

        BigDecimal despesasMes,

        VariacaoDTO variacaoDespesas,

        BigDecimal totalInvestido,

        VariacaoDTO variacaoInvestimentos,

        FaturaDTO faturaAtual,

        List<SaldoDTO> historicoSaldo,

        List<FluxoDTO> fluxoCaixa,

        List<GastoDiarioDTO> gastoDiario,

        List<GastoCategoriaDTO> gastoPorCategoria,

        List<LancamentoDTO> lancamentosRecentes

) {}