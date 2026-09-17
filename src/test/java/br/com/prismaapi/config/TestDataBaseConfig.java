package br.com.prismaapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@TestConfiguration
public class TestDataBaseConfig {

    @Value("${DATABASE_IP:localhost}")
    private String ip;

    @Value("${DATABASE_USER:postgres}")
    private String usuario;

    @Value("${DATABASE_PASSWORD:prismaAPI@2026}")
    private String senha;

    @Value("${DATABASE_PORT:5432}")
    private String porta;

    @Value("${DATABASE_NAME:prisma}")
    private String nome;

    private String getUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s", ip, porta, nome);
    }

    @Bean
    public DataSource dataSourceTest() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();

        dataSourceBuilder.driverClassName("org.postgresql.Driver");
        dataSourceBuilder.url(getUrl());
        dataSourceBuilder.username(usuario);
        dataSourceBuilder.password(senha);

        return dataSourceBuilder.build();
    }
}