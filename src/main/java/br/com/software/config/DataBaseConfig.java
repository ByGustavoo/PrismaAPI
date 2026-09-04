package br.com.software.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import javax.sql.DataSource;

@Configuration
@Profile({"dev", "prod"})
public class DataBaseConfig {

    @Value("${DATABASE_IP}")
    private String ip;

    @Value("${DATABASE_PORT}")
    private String porta;

    @Value("${DATABASE_NAME}")
    private String nome;

    @Value("${DATABASE_USER}")
    private String usuario;

    @Value("${DATABASE_PASSWORD}")
    private String senha;

    private String getUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s", ip, porta, nome);
    }

    @Bean
    public DataSource dataSource() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();

        dataSourceBuilder.driverClassName("org.postgresql.Driver");
        dataSourceBuilder.url(getUrl());
        dataSourceBuilder.username(usuario);
        dataSourceBuilder.password(senha);

        return dataSourceBuilder.build();
    }
}