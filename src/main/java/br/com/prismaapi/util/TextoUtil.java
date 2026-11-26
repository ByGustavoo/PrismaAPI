package br.com.prismaapi.util;

import java.text.Collator;
import java.util.Locale;

public final class TextoUtil {

    public static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    public static final Collator ORDEM_ALFABETICA = Collator.getInstance(PT_BR);

    private TextoUtil() {
    }

    public static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }

    public static String normalizar(String texto) {
        return texto.strip().toLowerCase(Locale.ROOT);
    }
}