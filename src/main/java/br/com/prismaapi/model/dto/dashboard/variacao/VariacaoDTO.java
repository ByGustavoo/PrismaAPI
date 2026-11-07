package br.com.prismaapi.model.dto.dashboard.variacao;

import br.com.prismaapi.enums.Tendencia;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Schema(description = "Variação de um total contra a janela anterior de mesmo tamanho.")
public record VariacaoDTO(

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "Variação em percentual. Ausente quando a janela anterior é zero ou pequena demais para a comparação dizer algo.")
        BigDecimal percentual,

        @Schema(description = "Diferença em reais entre o total atual e o da janela anterior.")
        BigDecimal diferenca,

        Tendencia tendencia

) {

    private static final int CASAS_DO_PERCENTUAL = 1;
    private static final BigDecimal LIMITE_DO_PERCENTUAL = new BigDecimal("999");
    private static final BigDecimal FAIXA_DA_ESTABILIDADE = new BigDecimal("0.05");

    public static VariacaoDTO entre(BigDecimal atual, BigDecimal anterior) {
        var base = anterior == null ? BigDecimal.ZERO : anterior;
        var diferenca = atual.subtract(base);

        if (base.signum() == 0) {
            return semPercentual(diferenca);
        }

        var percentual = diferenca
                .multiply(BigDecimal.valueOf(100))
                .divide(base.abs(), CASAS_DO_PERCENTUAL + 2, RoundingMode.HALF_UP);

        if (percentual.abs().compareTo(LIMITE_DO_PERCENTUAL) > 0) {
            return semPercentual(diferenca);
        }

        if (percentual.abs().compareTo(FAIXA_DA_ESTABILIDADE) <= 0) {
            return new VariacaoDTO(zeroPercentual(), emReais(diferenca), Tendencia.ESTAVEL);
        }

        var tendencia = percentual.signum() > 0 ? Tendencia.ALTA : Tendencia.BAIXA;
        return new VariacaoDTO(percentual.setScale(CASAS_DO_PERCENTUAL, RoundingMode.HALF_UP), emReais(diferenca), tendencia);
    }

    public static VariacaoDTO estavel() {
        return new VariacaoDTO(zeroPercentual(), emReais(BigDecimal.ZERO), Tendencia.ESTAVEL);
    }

    private static VariacaoDTO semPercentual(BigDecimal diferenca) {
        if (diferenca.signum() == 0) {
            return estavel();
        }

        var tendencia = diferenca.signum() > 0 ? Tendencia.ALTA : Tendencia.BAIXA;
        return new VariacaoDTO(null, emReais(diferenca), tendencia);
    }

    private static BigDecimal zeroPercentual() {
        return BigDecimal.ZERO.setScale(CASAS_DO_PERCENTUAL, RoundingMode.HALF_UP);
    }

    private static BigDecimal emReais(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}