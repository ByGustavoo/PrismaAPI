package br.com.prismaapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.deser.jdk.StringDeserializer;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATA_ESTRITA = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    @Bean
    public JacksonModule textoSemEspacosNasPontas() {
        return new SimpleModule("textoSemEspacosNasPontas").addDeserializer(String.class, new StdScalarDeserializer<>(String.class) {

            @Override
            public String deserialize(JsonParser parser, DeserializationContext contexto) {
                var texto = StringDeserializer.instance.deserialize(parser, contexto);
                return texto == null ? null : texto.strip();
            }
        });
    }

    @Bean
    public JacksonModule datasSemCorrecaoAutomatica() {
        return new SimpleModule("datasSemCorrecaoAutomatica").addDeserializer(LocalDate.class, new StdScalarDeserializer<>(LocalDate.class) {

            @Override
            public LocalDate deserialize(JsonParser parser, DeserializationContext contexto) {
                var texto = StringDeserializer.instance.deserialize(parser, contexto);

                try {
                    return LocalDate.parse(texto.strip(), DATA_ESTRITA);
                } catch (DateTimeParseException ex) {
                    return (LocalDate) contexto.handleWeirdStringValue(LocalDate.class, texto, "A data precisa existir e estar no formato yyyy-MM-dd!");
                }
            }
        });
    }
}