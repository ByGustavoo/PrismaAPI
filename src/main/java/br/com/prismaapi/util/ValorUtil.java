package br.com.prismaapi.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

public final class ValorUtil {

    private static final int CASAS_DA_FRACAO = 4;

    private ValorUtil() {
    }

    public static BigDecimal dinheiro(BigDecimal valor) {
        return zeroSeNulo(valor).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal fracao(BigDecimal parte, BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return BigDecimal.ZERO.setScale(CASAS_DA_FRACAO, RoundingMode.HALF_UP);
        }

        return parte.divide(total, CASAS_DA_FRACAO, RoundingMode.HALF_UP);
    }

    public static BigDecimal somar(Stream<BigDecimal> valores) {
        return valores.reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal somarEmReais(Stream<BigDecimal> valores) {
        return dinheiro(somar(valores));
    }

    public static BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}