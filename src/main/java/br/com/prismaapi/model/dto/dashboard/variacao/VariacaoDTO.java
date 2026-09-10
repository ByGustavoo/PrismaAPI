package br.com.prismaapi.model.dto.dashboard.variacao;

import br.com.prismaapi.enums.Tendencia;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Schema(description = "Variação de um total contra a janela anterior de mesmo tamanho.")
public record VariacaoDTO(

        BigDecimal percentual,

        Tendencia tendencia

) {

    private static final int CASAS_DO_PERCENTUAL = 1;
    private static final BigDecimal FAIXA_DA_ESTABILIDADE = new BigDecimal("0.05");

    public static VariacaoDTO entre(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.signum() == 0) {
            return estavel();
        }

        var percentual = atual.subtract(anterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(anterior.abs(), CASAS_DO_PERCENTUAL + 2, RoundingMode.HALF_UP);

        if (percentual.abs().compareTo(FAIXA_DA_ESTABILIDADE) <= 0) {
            return estavel();
        }

        var tendencia = percentual.signum() > 0 ? Tendencia.ALTA : Tendencia.BAIXA;
        return new VariacaoDTO(percentual.setScale(CASAS_DO_PERCENTUAL, RoundingMode.HALF_UP), tendencia);
    }

    public static VariacaoDTO estavel() {
        return new VariacaoDTO(BigDecimal.ZERO.setScale(CASAS_DO_PERCENTUAL, RoundingMode.HALF_UP), Tendencia.ESTAVEL);
    }
}