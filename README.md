<div align="center"> <br> 
  <img align="center" alt="prismaapi-spring" height="150" width="150" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/spring/spring-original.svg" />
</div> 

<br> 

<div align="center">
  API REST do <a href="https://github.com/ByGustavoo/PrismaWeb">PrismaWeb</a>, construída em Spring Boot. O Prisma é uma aplicação de finanças pessoais; esta API concentra as regras de negócio, os cálculos (saldo, faturas, previsão, avisos, relatórios), a persistência em PostgreSQL e os endpoints consumidos pela aplicação web.
</div> 

<br> <br> 

## 🚀 Ferramentas Utilizadas

* 🟥 Redis

* 🐳 Docker

* 🕊️ Flyway

* 📊 JaCoCo

* 📝 Log4j2

* 🔴 Lombok

* ☕️ Java 25

* 🧪 JUnit 5

* 🃏 Jackson 3

* 🐘 PostgreSQL 18

* ⚙️ GitHub Actions

* 🗺️ MapStruct 1.6.3

* 🟢 Spring Boot 4.1.1

* 🐘 Gradle 9.8.0 (Kotlin DSL)

* 📄 Springdoc OpenAPI 3.1.0 (Swagger UI)

<br> 

## ⚙️ Pré-requisitos

* JDK 25 instalada (o projeto não declara resolver de toolchain, então o Gradle não baixa a JDK sozinho)

* PostgreSQL e Redis acessíveis para os perfis `dev` e `prod`

* PostgreSQL próprio para os testes, separado do banco de `dev`

<br> 

## 🔐 Variáveis de Ambiente

Obrigatórias nos perfis `dev` e `prod` (usadas por `DataBaseConfig` e `RedisConfig`):

| Variável | Descrição |
|---|---|
| `DATABASE_IP` | Host do PostgreSQL |
| `DATABASE_PORT` | Porta do PostgreSQL |
| `DATABASE_NAME` | Nome do banco |
| `DATABASE_USER` | Usuário do banco |
| `DATABASE_PASSWORD` | Senha do banco |
| `REDIS_IP` | Host do Redis |
| `REDIS_PORT` | Porta do Redis |
| `REDIS_PASSWORD` | Senha do Redis (opcional) |

No perfil `test` as variáveis `DATABASE_*` são lidas por `TestDataBaseConfig`, que assume
`localhost:5432/prisma` quando elas não estão definidas. O perfil desliga o cache, então os testes
não precisam de Redis.

<br> 

## ▶️ Como Executar

```bash
# Sobe o PostgreSQL na 5432 e o Redis na 6379
docker compose -f docker-compose-postgres.yml up -d

# Ambiente de desenvolvimento (porta 9017)
./gradlew bootRun --args="--spring.profiles.active=dev"

# Ambiente de produção (porta 9027)
./gradlew bootRun --args="--spring.profiles.active=prod"
```

No IntelliJ IDEA, as configurações equivalentes estão em `.run/`, já com as variáveis apontando
para o PostgreSQL e o Redis do `docker-compose-postgres.yml`.

A aplicação sobe sob o context path `/PrismaAPI`. Em desenvolvimento, a documentação
fica em `http://localhost:9017/PrismaAPI/swagger-ui.html`.

<br> 

## 🔌 API

São 16 recursos sob `/PrismaAPI/v1`, com 64 endpoints. Cada um é documentado no Swagger pela
interface `<Recurso>Docs`.

| Recurso | Endpoints | O que faz |
|---|---|---|
| `/avisos` | 8 | Avisos derivados dos dados, leitura, adiamento e preferências |
| `/cartoes` | 4 | Cartões de crédito, débito e vale |
| `/categorias` | 1 | Catálogo fixo de categorias de receita e despesa |
| `/compras-parceladas` | 6 | Compras no cartão e pagamento antecipado de parcela |
| `/contas` | 5 | Contas e origens dos lançamentos |
| `/dashboard` | 1 | Resumo do período: saldo, patrimônio, fluxo e categorias |
| `/faturas` | 4 | Faturas calculadas, detalhe e pagamento |
| `/investimentos` | 12 | Carteira, extrato, aportes, resgates, saldos e ações por quantidade |
| `/lancamentos` | 5 | Receitas, despesas e transferências, com listagem paginada |
| `/metas` | 5 | Metas de compra e histórico de preços |
| `/orcamentos` | 4 | Limite mensal por categoria e visão geral do mês |
| `/previsao` | 1 | Projeção do resto do mês e dos meses seguintes |
| `/recorrentes` | 4 | Receitas e despesas recorrentes, com lançamento automático |
| `/relatorios` | 1 | Resumo por recorte de datas |
| `/sistema` | 1 | Versão e data do build |
| `/telas` | 2 | Telas ocultas no menu |

<br> 

## 🧪 Testes e Build

```bash
# Compilar
./gradlew classes

# Testes (gera o relatório JaCoCo em build/reports/jacoco)
./gradlew test

# Build sem testes
./gradlew clean build -x test
```

Os testes rodam sobre um banco populado pela carga de `src/test/resources/db/test`, aplicada pelo
Flyway só no perfil `test`. Aponte as variáveis `DATABASE_*` para um banco próprio, e não para o de
desenvolvimento.

<br> 

## 🐳 Docker

```bash
# Sobe um PostgreSQL local na porta 5432 e um Redis na 6379
docker compose -f docker-compose-postgres.yml up -d

# Sobe a API em produção na porta 9027, com Redis próprio, lendo as variáveis de um .env ao lado
docker compose -f docker-compose-prismaapi.yml up -d
```

O `Dockerfile` constrói em dois estágios: `gradle:jdk25` gera o jar com
`gradle build -x test` e `eclipse-temurin:25-jre` executa `PrismaAPI.jar`.

<br> 

## 🤖 Integração Contínua

| Workflow | Gatilho | O que faz |
|---|---|---|
| `workflow.yml` | Pull Request para `main` | Sobe um PostgreSQL de serviço e roda `build jacocoTestReport` |
| `release.yml` | PR mergeado em `main` (ou manual) | Sobe a versão a partir da última tag `vX.Y.Z`, publica a imagem multi-arch no Docker Hub e cria a tag e a Release |

O incremento é de patch, ou de minor/major quando o PR tem o rótulo `release:minor` ou
`release:major`. A versão publicada é a que `GET /v1/sistema/versao` devolve.

<br> 

## 📁 Estrutura

```
src/main/java/br/com/prismaapi
├── PrismaAPIApplication.java   # Classe de inicialização
├── config                      # CorsConfig, DataBaseConfig, JacksonConfig, RedisConfig e cache/
├── controller                  # Um pacote por recurso: <Recurso>Controller + <Recurso>Docs
├── enums                       # Enums do domínio
├── exceptions                  # Exceções de negócio, dto e GlobalExceptionHandler
├── model                       # dto, entity e mapper (MapStruct)
├── repository                  # Repositórios Spring Data, Specifications e busca sem acento
├── service                     # Regras de negócio e cálculos
└── util                        # ValorUtil, TextoUtil e PeriodoUtil

src/main/resources
├── application.yaml            # Configuração por perfil (dev, prod, test)
├── banner.txt                  # Banner com versão e data do build
├── log4j2.xml                  # Configuração de logging
└── db/migration                # Migrations do Flyway

src/test/java/br/com/prismaapi
├── config                      # AbstractTest, AbstractControllerTest e TestDataBaseConfig
├── controller                  # Um teste por endpoint: <Recurso>ControllerTest
├── repository                  # Um teste por repositório: <Recurso>RepositoryTest
└── service                     # Um teste por service: <Recurso>ServiceTest

src/test/resources
└── db/test                     # Massa de dados aplicada só no perfil test
```

<br> 
 
## 🖥️ Desenvolvedor

### 🔵 LinkedIn: [Gustavo Correa](https://www.linkedin.com/in/gustavo-chauar-correa-946168269/)
