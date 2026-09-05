# PrismaAPI

## O que é

API REST do PrismaWeb, em Spring Boot. Concentra as regras de negócio, a persistência
em PostgreSQL e os endpoints consumidos pela aplicação web.

O repositório nasceu de um template Spring Boot próprio e ainda está em fase inicial:
há apenas a classe de inicialização e a configuração de datasource. Nenhum domínio,
controller ou migration foi escrito até agora.

## Tipo

Backend / API — release: branch + Pull Request (`origin`: `ByGustavoo/PrismaAPI`).

## Stack

- Java 21 (toolchain declarada em `build.gradle.kts`)
- Spring Boot 4.1.1, Gradle 9.6.1 com Kotlin DSL
- PostgreSQL + Flyway (`spring-boot-starter-flyway`, `flyway-database-postgresql`)
- Spring Data JPA, Spring Validation, Spring Web MVC
- MapStruct 1.6.3 + Lombok (com `lombok-mapstruct-binding`)
- Log4j2 — Logback e `spring-boot-starter-logging` são **excluídos** em
  `configurations.configureEach`; não reintroduza dependências que os tragam de volta
- Springdoc OpenAPI 3.1.0 (Swagger UI)
- JUnit 5 + `spring-boot-starter-test`; cobertura via JaCoCo

## Estrutura

```
src/main/java/br/com/software/
  PrismaAPIApplication.java   # ponto de entrada @SpringBootApplication
  config/DataBaseConfig.java  # DataSource montado via env vars, perfis dev e prod
src/main/resources/
  application.yaml            # config base + perfis dev, prod e test
  log4j2.xml                  # console em dev, arquivo rotativo em /app/logs em prod
  db/migration/               # migrations Flyway, padrão V<versão>__<Descrição>.sql
.run/                         # run configurations do IntelliJ (ignoradas pelo Git)
```

## Comandos

| Objetivo | Comando |
|---|---|
| Rodar em dev (porta 9017) | `./gradlew bootRun --args="--spring.profiles.active=dev"` |
| Rodar em prod (porta 9027) | `./gradlew bootRun --args="--spring.profiles.active=prod"` |
| Testes + relatório JaCoCo | `./gradlew test` |
| Build sem testes | `./gradlew clean build -x test` |

Os comandos vêm das run configurations em `.run/` e do `build.gradle.kts`. **Não foi
possível executá-los nesta máquina para validar**: o Gradle falha ao iniciar o Worker
Daemon (`ClassNotFoundException: GradleWorkerMain`) quando rodado pelo agente — é
limitação do ambiente sandbox, não do projeto.

`tasks.test` é `finalizedBy(jacocoTestReport)`, então `./gradlew test` sempre gera o
relatório HTML em `build/reports/jacoco`.

## Perfis e ambiente

- `dev` — porta 9017, `format_sql` ligado, `org.hibernate.SQL` em DEBUG e binder em TRACE
- `prod` — porta 9027, log em arquivo rotativo com retenção de 30 dias
- `test` — sem datasource; `DataBaseConfig` é `@Profile({"dev","prod"})`, então testes
  não sobem o banco por essa via
- Variáveis obrigatórias em `dev` e `prod`: `DATABASE_IP`, `DATABASE_PORT`,
  `DATABASE_NAME`, `DATABASE_USER`, `DATABASE_PASSWORD`. Sem elas a aplicação não sobe.
  As run configurations do IntelliJ já as definem apontando para um Postgres local.

## Convenções

- Pacote raiz `br.com.prismaapi`; código em português nos identificadores de negócio
  (ver `DataBaseConfig`: `ip`, `porta`, `nome`, `usuario`, `senha`)
- `spring.jpa.open-in-view: false` — carregue o que a resposta precisa dentro da
  transação; não conte com lazy loading no controller
- `include-stacktrace: never` — erros não vazam stacktrace na resposta
- Mapeamento entidade ↔ DTO com MapStruct, não manualmente
- Arquitetura limpa: use a skill `java-clean-architecture` ao criar controllers,
  use cases, entidades ou repositórios

## Identidade da aplicação

O rename do template para PrismaAPI está concluído. Os nomes abaixo andam juntos —
mudar um sem os outros quebra rotas, migrations ou logs:

- `settings.gradle.kts`: `rootProject.name = "PrismaAPI"`
- `application.yaml`: `context-path: /PrismaAPI` (toda rota vive sob esse prefixo, ex.:
  Swagger UI em `http://localhost:9017/PrismaAPI/swagger-ui.html`),
  `spring.application.name: prismaapi`, Flyway `schema` e `default-schema: prismaapi`
- `log4j2.xml`: em `prod`, log rotativo em `/app/logs/PrismaAPI.log`
- `build.gradle.kts`: o JaCoCo exclui `**/PrismaAPIApplication.class` e `**/config/**`

## Estado atual

- Migrations em `src/main/resources/db/migration`. A primeira,
  `V1.0__CreateTables.sql`, existe mas ainda está vazia — o padrão de nome a seguir é
  `V<versão>__<Descrição>.sql`. O Flyway roda com `baseline-on-migrate: true` sobre o
  schema `prismaapi`.
- Não há testes: o único teste do template foi removido junto com o rename. Um teste de
  contexto só passa se houver DataSource — `DataBaseConfig` é `@Profile({"dev","prod"})`,
  então o perfil `test` precisará de um datasource próprio (Testcontainers ou H2).
