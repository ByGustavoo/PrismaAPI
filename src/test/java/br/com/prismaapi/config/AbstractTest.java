package br.com.prismaapi.config;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Predicate;

@Transactional
@ActiveProfiles("test")
@Import(value = TestDataBaseConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractTest {

    protected <T> T buscar(JpaRepository<T, UUID> repository, Predicate<T> filtro) {
        return repository.findAll()
                .stream()
                .filter(filtro)
                .findFirst()
                .orElseThrow();
    }
}