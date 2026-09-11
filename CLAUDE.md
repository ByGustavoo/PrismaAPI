# PrismaAPI

## O que é

API REST do PrismaWeb, em Spring Boot. Concentra as regras de negócio, a persistência em
PostgreSQL e os endpoints consumidos pela aplicação web. O contrato completo — rota, corpo,
status, validação e regra de cálculo de cada endpoint — vive no `API_CONTRACT.md` do PrismaWeb.
Mudou comportamento aqui, muda lá no mesmo trabalho.

## Tipo

Backend / API — release: branch + Pull Request (`origin`: `ByGustavoo/PrismaAPI`).

## Stack

- Java 21 (toolchain declarada em `build.gradle.kts`)
- Spring Boot 4.1.1, Gradle com Kotlin DSL
- PostgreSQL + Flyway (`spring-boot-starter-flyway`, `flyway-database-postgresql`)
- Spring Data JPA, Spring Validation, Spring Web MVC, Jackson 3 (`tools.jackson`)
- MapStruct 1.6.3 + Lombok (com `lombok-mapstruct-binding`)
- Log4j2 — Logback e `spring-boot-starter-logging` são **excluídos** em
  `configurations.configureEach`; não reintroduza dependências que os tragam de volta
- Springdoc OpenAPI 3.1.0 (Swagger UI)
- JUnit 5 + `spring-boot-starter-test`; cobertura via JaCoCo

## Estrutura

```
src/main/java/br/com/prismaapi/
  PrismaAPIApplication.java
  config/          CorsConfig (origens por propriedade), DataBaseConfig, JacksonConfig
  controller/      um pacote por recurso: <Recurso>Controller + <Recurso>Docs (mapeamento e OpenAPI)
  enums/           enums do domínio
  exceptions/      uma RuntimeException por falha de negócio, dto/ e handler/GlobalExceptionHandler
  model/dto/       DTOs de entrada e saída, e projeções por recurso
  model/entity/    entidades JPA
  model/mapper/    mappers MapStruct
  repository/      repositórios Spring Data e Specifications
  service/         regras de negócio e cálculos (faturas, saldo, previsão, dashboard, relatórios, avisos)
src/main/resources/
  application.yaml config base + perfis dev, prod e test
  log4j2.xml       console em dev, arquivo rotativo em /app/logs em prod
  db/migration/    V1.0__CreateTables.sql (esquema) e V1.1__InsertCategorias.sql (catálogo de categorias)
.run/              run configurations do IntelliJ (ignoradas pelo Git)
```

## Comandos

| Objetivo | Comando |
|---|---|
| Rodar em dev (porta 9017) | `./gradlew bootRun --args="--spring.profiles.active=dev"` |
| Rodar em prod (porta 9027) | `./gradlew bootRun --args="--spring.profiles.active=prod"` |
| Compilar | `./gradlew classes` |
| Testes + relatório JaCoCo | `./gradlew test` |
| Build sem testes | `./gradlew clean build -x test` |

Com a aplicação rodando em dev, o `spring-boot-devtools` está no classpath: `./gradlew classes`
recompila em poucos segundos, o contexto reinicia sozinho e migrações novas são aplicadas no
restart. É o caminho mais curto para validar uma mudança contra o PrismaWeb.

`tasks.test` é `finalizedBy(jacocoTestReport)`, então `./gradlew test` sempre gera o
relatório HTML em `build/reports/jacoco`.

## Perfis e ambiente

- `dev` — porta 9017, `format_sql` ligado, `org.hibernate.SQL` em DEBUG e binder em TRACE;
  CORS aceita qualquer porta de `http://localhost` e `http://127.0.0.1`
- `prod` — porta 9027, log em arquivo rotativo com retenção de 30 dias; CORS só na origem de
  `prismaapi.cors.origens-permitidas`
- `test` — sem datasource; `DataBaseConfig` é `@Profile({"dev","prod"})`, então testes
  não sobem o banco por essa via
- Variáveis obrigatórias em `dev` e `prod`: `DATABASE_IP`, `DATABASE_PORT`,
  `DATABASE_NAME`, `DATABASE_USER`, `DATABASE_PASSWORD`. Sem elas a aplicação não sobe.
  As run configurations do IntelliJ já as definem apontando para um Postgres local.
- `prismaapi.cors.origens-permitidas` recebe padrões de origem separados por vírgula
  (`allowedOriginPatterns`). O perfil dev libera as portas locais porque um segundo Vite ou o
  acesso por IP recebiam `403` no preflight.

## Convenções

- Pacote raiz `br.com.prismaapi`; código em português nos identificadores de negócio
- `spring.jpa.open-in-view: false` — carregue o que a resposta precisa dentro da
  transação; não conte com lazy loading no controller
- Rotas em português, em kebab-case sem acento (`/v1/contas/origens`,
  `/v1/despesas-recorrentes`, `/v1/metas/{id}/precos`). Rota é contrato com o PrismaWeb: mudou
  aqui, muda no `src/api/rotasApi.ts` e no `API_CONTRACT.md` de lá no mesmo trabalho
- Métodos de controller e de `*Docs` levam o nome da ação, não do verbo HTTP: `listarCartoes`,
  `buscarFatura`, `salvarCartao`, `atualizarCartao`, `deletarCartao`
- Mapeamento entidade ↔ DTO com MapStruct, não manualmente. Um método de um argumento que devolve o
  próprio DTO vira "propriedade" para o MapStruct — não declare wither em record mapeado
- Arquitetura limpa: use a skill `java-clean-architecture` ao criar controllers,
  use cases, entidades ou repositórios
- **Resposta de erro não vaza detalhe interno.** `detail` é a frase para o toast; `errors` só
  existe na validação de campo, com uma mensagem por campo (a de ausência vence quando o campo
  quebra mais de uma regra). Nas falhas técnicas o `GlobalExceptionHandler` loga a exceção e
  responde uma frase fixa — nunca texto de exceção, SQL ou assinatura de método
- **Data de corpo JSON é estrita.** O `JacksonConfig` troca o desserializador de `LocalDate` por um
  `uuuu-MM-dd` com `ResolverStyle.STRICT`: `2026-02-30` responde `400`, em vez de ser gravada como
  `2026-02-28`. Query params já são estritos pelo `@DateTimeFormat` do Spring
- **Campo opcional do contrato é omitido quando nulo** (`@JsonInclude(NON_NULL)` no componente do
  record). Campo que o contrato declara como "presente e `null`" — `categoria` do lançamento e do
  item de fatura — usa `Include.ALWAYS`, mesmo com `NON_NULL` na classe

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

- Todos os endpoints do `API_CONTRACT.md` estão implementados, com o esquema em `V1.0` e o catálogo
  fixo de categorias em `V1.1`. A migração de categorias usa `ON CONFLICT (nome, tipo) DO NOTHING`
  para rodar sobre bancos que já tinham as categorias inseridas à mão.
- Regras de lançamento no `LancamentoService`: categoria do lado do lançamento, forma de pagamento
  compatível com a origem (cartão exige `CARTAO_CREDITO`, conta recusa), cartão de débito não é
  origem e `PAGO` não pode ter data futura. Compra parcelada recusa categoria de receita e primeira
  parcela antes do mês da compra.
- `DespesaRecorrenteService` devolve `proximoVencimento` avançado pela frequência até hoje
  (`Frequencia.proximaOcorrencia`, a mesma conta da previsão); o banco guarda a data informada.
- `faturaAtual` do dashboard vem `null` quando nenhum cartão teve movimento no mês.
- Não há testes. Um teste de contexto só passa se houver DataSource — `DataBaseConfig` é
  `@Profile({"dev","prod"})`, então o perfil `test` precisará de um datasource próprio
  (Testcontainers ou H2).