package br.com.prismaapi.config;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test")
@Import(value = TestDataBaseConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractTest {

}