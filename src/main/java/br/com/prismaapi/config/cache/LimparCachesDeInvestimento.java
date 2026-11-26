package br.com.prismaapi.config.cache;

import org.springframework.cache.annotation.CacheEvict;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@CacheEvict(value = {"avisos", "contas", "dashboard", "investimentos", "lancamentos", "previsao", "relatorios"}, allEntries = true)
public @interface LimparCachesDeInvestimento {}