package br.com.prismaapi.model.dto.orcamento;

import br.com.prismaapi.model.dto.dashboard.gasto.categoria.GastoCategoriaDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Schema(description = "Representa o consumo consolidado dos orçamentos num mês.")
public record VisaoGeralOrcamentoDTO(

        @JsonFormat(pattern = "yyyy-MM")
        YearMonth mes,

        BigDecimal planejado,

        BigDecimal gasto,

        BigDecimal restante,

        BigDecimal consumo,

        Integer diasRestantes,

        Integer diasDecorridos,

        Integer diasNoMes,

        List<ConsumoOrcamentoDTO> itens,

        List<GastoCategoriaDTO> foraDoOrcamento

) {}