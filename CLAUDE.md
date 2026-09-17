# PrismaAPI

## O que é

API REST do PrismaWeb, em Spring Boot. Concentra as regras de negócio, a persistência em
PostgreSQL e os endpoints consumidos pela aplicação web. O contrato completo — rota, corpo,
status, validação e regra de cálculo de cada endpoint — vive no `API_CONTRACT.md` do PrismaWeb.
Mudou comportamento aqui, muda lá no mesmo trabalho.

## Tipo

Backend / API — release: branch + Pull Request (`origin`: `ByGustavoo/PrismaAPI`).

## Stack

- Java 25 (toolchain declarada em `build.gradle.kts`)
- Spring Boot 4.1.1, Gradle com Kotlin DSL
- PostgreSQL + Flyway (`spring-boot-starter-flyway`, `flyway-database-postgresql`)
- Spring Data JPA, Spring Validation, Spring Web MVC, Jackson 3 (`tools.jackson`)
- MapStruct 1.6.3 + Lombok (com `lombok-mapstruct-binding`)
- Log4j2 — Logback e `spring-boot-starter-logging` são **excluídos** em
  `configurations.configureEach`; não reintroduza dependências que os tragam de volta
- Springdoc OpenAPI 3.1.0 (Swagger UI)
- JUnit 5 + `spring-boot-starter-test` e os starters de teste por módulo do Spring Boot 4
  (`flyway-test`, `webmvc-test`, `data-jpa-test`, `validation-test`); cobertura via JaCoCo
- Docker (build em dois estágios) e GitHub Actions (build no PR, imagem no push para `main`)

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
  banner.txt       banner do start, com ${build.version} e ${build.data}
  log4j2.xml       console em dev, arquivo rotativo em /app/logs em prod
  db/migration/    V1.0__CreateTables.sql (esquema) e V1.1__InsertCategorias.sql (catálogo de categorias)
.run/              run configurations do IntelliJ (ignoradas pelo Git)
.github/workflows/ workflow.yml (build no PR) e release.yml (imagem no push para main)
Dockerfile         build em dois estágios: gradle:jdk25 compila, eclipse-temurin:25-jre executa
docker-compose-postgres.yml   PostgreSQL 18 local na 5432
docker-compose-prismaapi.yml  a imagem do Docker Hub na 9027, variáveis vindas do .env
src/test/java/br/com/prismaapi/
  config/          AbstractTest, AbstractControllerTest e TestDataBaseConfig (datasource dos testes)
  repository/      um pacote por repositório, espelhando o main: <Recurso>RepositoryTest
src/test/resources/
  db/test/         V1.2__PopularBanco.sql, dados de exemplo aplicados só no perfil test
```

São 15 recursos sob `/v1`, com 47 endpoints no total: `avisos`, `cartoes`, `categorias`,
`compras-parceladas`, `contas`, `dashboard`, `despesas-recorrentes`, `faturas`, `investimentos`,
`lancamentos`, `metas`, `orcamentos`, `previsao`, `relatorios` e `sistema`. O esquema tem 11
tabelas, todas em `prisma`.

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
- `test` — só para `./gradlew test`. O datasource vem do `TestDataBaseConfig` (as mesmas variáveis
  `DATABASE_*`, com padrão `localhost:5432/prisma`), e o Flyway lê `classpath:db/migration` e
  `classpath:db/test`: a `V1.2__PopularBanco.sql` popula o banco com um ano de dados relativos à data em
  que roda. Aponte as variáveis para um banco próprio, nunca para o de dev, que não conhece a `V1.2`
- Variáveis obrigatórias em `dev` e `prod`: `DATABASE_IP`, `DATABASE_PORT`,
  `DATABASE_NAME`, `DATABASE_USER`, `DATABASE_PASSWORD`. Sem elas a aplicação não sobe.
  As run configurations do IntelliJ já as definem apontando para um Postgres local.
- `prismaapi.cors.origens-permitidas` recebe padrões de origem separados por vírgula
  (`allowedOriginPatterns`). O padrão da config base é `http://localhost:5173`, a origem do Vite do
  PrismaWeb; o perfil dev a substitui pelas portas locais porque um segundo Vite ou o acesso por IP
  recebiam `403` no preflight. Em prod, sobrescreva a propriedade em vez de editar o YAML.

## Docker e CI

- `Dockerfile` — `gradle:jdk25` roda `gradle build -x test` e `eclipse-temurin:25-jre` executa o jar
  como `PrismaAPI.jar`. O `COPY build/libs/*.jar` só casa com um arquivo porque a task `jar` está
  desabilitada no `build.gradle.kts`; se você reabilitá-la, o build da imagem passa a copiar dois jars.
- `docker-compose-postgres.yml` sobe um PostgreSQL 18 local (`prisma`/`postgres`) na 5432 — é o banco
  que os perfis dev e test assumem por padrão. `docker-compose-prismaapi.yml` sobe a imagem publicada
  no perfil `prod`, na 9027, com `TZ=GMT-3`, as variáveis `DATABASE_*` vindas de um `.env` ao lado e
  `./logs` montado em `/app/logs`, que é onde o `log4j2.xml` grava em prod.
- `.github/workflows/workflow.yml` roda em Pull Request para `main`: sobe um PostgreSQL 18 de serviço
  e executa `./gradlew build jacocoTestReport`. Como os testes de repositório precisam da `V1.2`, o
  banco do CI é criado do zero pelo Flyway a cada execução.
- `.github/workflows/release.yml` roda no push para `main`: extrai a versão com um `grep '^version'`
  no `build.gradle.kts` e publica a imagem no Docker Hub nessa tag e em `latest`. Ou seja, o
  `version = "1.0.0"` do Gradle é ao mesmo tempo a tag da imagem e o que `GET /v1/sistema/versao`
  devolve — subir a versão é editar essa linha, e uma linha `version` fora do formato quebra o release.

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
- **Texto de corpo JSON chega sem espaço nas pontas.** O `JacksonConfig` troca o desserializador de
  `String` por um que aplica `strip()`: nenhuma validação nem regra de negócio precisa aparar o
  valor, e `"   "` num campo `@NotBlank` vira `""` antes de ser validado
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
  `spring.application.name: prismaapi`, Flyway `schemas` e `default-schema: prisma`
- Entidades JPA: `@Table(..., schema = "prisma")`, o mesmo schema do Flyway
- `log4j2.xml`: em `prod`, log rotativo em `/app/logs/PrismaAPI.log`
- `build.gradle.kts`: o JaCoCo exclui `**/PrismaAPIApplication.class`, `**/config/**`, `**/enums/**`,
  `**/model/**` e `**/exceptions/**` — o relatório mede controller, service e repository
- `Dockerfile` e `docker-compose-prismaapi.yml`: o jar copiado e o container se chamam `PrismaAPI`

## Estado atual

- Todos os endpoints do `API_CONTRACT.md` estão implementados, com o esquema em `V1.0` e o catálogo
  fixo de 17 categorias em `V1.1` (`token_cor` de 1 a 16, sem cor repetida dentro do mesmo tipo). A
  migração de categorias usa `ON CONFLICT (nome, tipo) DO UPDATE SET token_cor` para rodar sobre bancos
  que já tinham as categorias inseridas à mão.
- Só lançamento `PAGO` mexe em `contas.saldo`, na mesma transação: `POST` aplica o efeito, `PUT` desfaz
  o antigo e aplica o novo, `DELETE` desfaz — cada passo só se o lançamento em questão é `PAGO`.
  `PENDENTE` e `AGENDADO` não mexem, qualquer que seja a data, e lançamento em cartão não mexe em conta.
  Como `PAGO` não aceita data futura, não há tarefa agendada.
- A linha do saldo (`SaldoService`, usada por dashboard e relatórios) reconstrói o passado só com os
  `PAGO` e projeta o futuro com os agendados; desconta despesa em cartão de crédito na data da compra e
  cada parcela na data de vencimento da fatura em que cai.
- O resto do mês da previsão soma os lançamentos não pagos até o fim do mês, inclusive os vencidos.
- `TipoConta` define a `FinalidadeConta`: `EMERGENCIA`, `POUPANCA` e `PREVIDENCIA` são `RESERVA`. A
  evolução de conta (`EvolucaoContaService`) não tem tabela: sai dos lançamentos `PAGO` da janela de doze
  meses, e o saldo inicial é o saldo de hoje menos o efeito deles.
- Investimento tem série em `movimentacoes_investimento` (`APORTE` soma valor, `RENDIMENTO` guarda o
  saldo informado). `aportado`, `valor_atual`, `data_inicio` e `data_ultima_movimentacao` de
  `investimentos` são o resumo dessa série, regravado a cada movimentação; o `PUT` não mexe em valores.
- `PrevisaoService` projeta o resto do mês corrente e os meses cheios a partir das médias dos três meses
  fechados, contando recorrentes para trás e para frente de `proximoVencimento`
  (`Frequencia.ocorrenciaAnterior`).
- Regras de lançamento no `LancamentoService`: categoria do lado do lançamento, forma de pagamento
  compatível com a origem (cartão exige `CARTAO_CREDITO`, conta recusa), cartão de débito não é
  origem e `PAGO` não pode ter data futura. Compra parcelada recusa categoria de receita e primeira
  parcela antes do mês da compra.
- `DespesaRecorrenteService` devolve `proximoVencimento` avançado pela frequência até hoje
  (`Frequencia.proximaOcorrencia`, a mesma conta da previsão); o banco guarda a data informada.
- `faturaAtual` do dashboard vem `null` quando nenhum cartão teve movimento no mês.
- `GET /v1/sistema/versao` lê o `BuildProperties` gerado pelo `buildInfo()` do `build.gradle.kts`, que
  grava `build.time` já truncado em segundos e um `build.data` extra, a mesma data em
  `dd/MM/uuuu - HH:mm:ss`. O `banner.txt` mostra essas duas linhas no start porque o
  `spring.config.import` carrega o `build-info.properties` no Environment antes do banner — é a única
  forma de o banner enxergar valores do build. O import é `optional:`, então a falta do arquivo não
  derruba a configuração; quem derruba é o `SistemaService`, que injeta `BuildProperties`, e esse bean
  só existe quando o `build-info.properties` está no classpath. Rodar a aplicação fora do Gradle, sem
  esse arquivo, impede o contexto de subir e deixa os `${build.*}` do banner sem resolver.
- O banner sai pelo `System.out`, que no Windows usa a página de código do console e corrompe os
  acentos — as linhas de log escapam disso porque o Log4j2 grava UTF-8 direto. Por isso o `bootRun`
  leva `-Dstdout.encoding=UTF-8` e `-Dstderr.encoding=UTF-8` nos `jvmArgs`, e o `ENTRYPOINT` do
  `Dockerfile` leva os mesmos dois. Texto com acento é para funcionar; não troque a palavra.
- Os testes cobrem só os repositórios: um teste por método próprio, com `assertDoesNotThrow`, rodando
  sobre o banco populado pela `V1.2`. Eles pegaram a falta da extensão `unaccent`, usada pelas buscas
  de lançamentos e metas e criada no topo da `V1.0`. O `AbstractControllerTest` já existe, com os
  atalhos de `MockMvc` por status, mas ainda não tem nenhum teste de controller em cima dele.