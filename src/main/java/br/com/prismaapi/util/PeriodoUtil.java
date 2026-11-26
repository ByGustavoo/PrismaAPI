package br.com.prismaapi.util;

import br.com.prismaapi.exceptions.RequisicaoInvalidaException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PeriodoUtil {

    private PeriodoUtil() {
    }

    public static <T extends Comparable<? super T>> void validar(T inicio, T fim) {
        if (inicio != null && fim != null && inicio.compareTo(fim) > 0) {
            log.warn("O período informado é inválido!");
            throw new RequisicaoInvalidaException("O período informado é inválido!");
        }
    }
}