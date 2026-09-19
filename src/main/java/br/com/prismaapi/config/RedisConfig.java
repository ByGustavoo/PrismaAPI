package br.com.prismaapi.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import tools.jackson.databind.jsontype.TypeIdResolver;
import tools.jackson.databind.jsontype.impl.ClassNameIdResolver;
import tools.jackson.databind.jsontype.impl.DefaultTypeResolverBuilder;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Configuration
@Profile({"dev", "prod"})
public class RedisConfig implements CachingConfigurer {

    @Value("${REDIS_IP}")
    private String ip;

    @Value("${REDIS_PORT}")
    private Integer porta;

    @Value("${REDIS_PASSWORD:}")
    private String senha;

    private static final Duration TEMPO_LIMITE = Duration.ofSeconds(2);
    private final AtomicLong geracao = new AtomicLong(System.currentTimeMillis());

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(ip, porta);

        config.setPassword(senha);

        var opcoes = ClientOptions.builder()
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .socketOptions(SocketOptions.builder().connectTimeout(TEMPO_LIMITE).build())
                .build();

        var cliente = LettuceClientConfiguration.builder()
                .commandTimeout(TEMPO_LIMITE)
                .clientOptions(opcoes)
                .build();

        return new LettuceConnectionFactory(config, cliente);
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(RedisConnectionFactory redisConnectionFactory) {
        return builder -> builder.cacheWriter(RedisCacheWriter.create(redisConnectionFactory, RedisCacheWriter.RedisCacheWriterConfigurer::immediateWrites));
    }

    @Override
    public KeyGenerator keyGenerator() {
        return (_, metodo, parametros) -> "%s:%s:%s.%s%s".formatted(LocalDate.now(), geracao.get(), metodo.getDeclaringClass().getSimpleName(), metodo.getName(), Arrays.toString(parametros));
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException ex, Cache cache, Object chave) {
                log.warn("Falha ao ler o cache! Buscando no banco... - Cache: {} - Chave: {} - Erro: {}", cache.getName(), chave, ex.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException ex, Cache cache, Object chave, Object valor) {
                log.warn("Falha ao gravar no cache! - Cache: {} - Chave: {} - Erro: {}", cache.getName(), chave, ex.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException ex, Cache cache, Object chave) {
                log.error("Falha ao remover do cache! Renovando as chaves... - Cache: {} - Chave: {} - Erro: {}", cache.getName(), chave, ex.getMessage());
                geracao.incrementAndGet();
            }

            @Override
            public void handleCacheClearError(RuntimeException ex, Cache cache) {
                log.error("Falha ao limpar o cache! Renovando as chaves... - Cache: {} - Erro: {}", cache.getName(), ex.getMessage());
                geracao.incrementAndGet();
            }
        };
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        var validador = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("br.com.prismaapi.")
                .allowIfSubType("java.")
                .build();

        var tipagem = new DefaultTypeResolverBuilder(validador, DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY) {

            @Override
            public boolean useForType(JavaType tipo) {
                return !tipo.isPrimitive();
            }

            @Override
            protected TypeIdResolver idResolver(DatabindContext contexto, JavaType tipoBase, PolymorphicTypeValidator validador, Collection<NamedType> subtipos, boolean serializacao, boolean desserializacao) {
                return new ClassNameIdResolver(tipoBase, subtipos, validador) {

                    @Override
                    protected String _idFrom(DatabindContext contexto, Object valor, Class<?> classe) {
                        return valor instanceof List<?> ? ArrayList.class.getName() : super._idFrom(contexto, valor, classe);
                    }
                };
            }
        };

        var serializador = GenericJacksonJsonRedisSerializer.builder()
                .customize(mapper -> mapper.setDefaultTyping(tipagem))
                .build();

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(serializador));
    }
}