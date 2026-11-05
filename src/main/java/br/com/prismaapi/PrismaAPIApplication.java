package br.com.prismaapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class PrismaAPIApplication {

	static void main(String[] args) {
		SpringApplication.run(PrismaAPIApplication.class, args);
	}
}