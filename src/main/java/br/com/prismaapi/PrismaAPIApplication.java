package br.com.prismaapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class PrismaAPIApplication {

	static void main(String[] args) {
		SpringApplication.run(PrismaAPIApplication.class, args);
	}
}