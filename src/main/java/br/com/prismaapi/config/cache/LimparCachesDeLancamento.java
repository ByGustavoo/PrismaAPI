package br.com.prismaapi.config.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@CacheEvict(value = {"avisos", "cartoes", "contas", "dashboard", "faturas", "lancamentos", "orcamentos", "previsao", "recorrentes", "relatorios"}, allEntries = true)
public @interface LimparCachesDeLancamento {

    @AliasFor(annotation = CacheEvict.class)
    String condition() default "";
}