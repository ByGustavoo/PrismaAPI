<div align="center"> <br> 
  <img align="center" alt="guru-java" height="150" width="150" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/spring/spring-original.svg" />
</div> 

<br> 

<div align="center">
  API REST do PrismaWeb, construída em Spring Boot. Concentra as regras de negócio, a persistência e os endpoints consumidos pela aplicação web.
</div> 

 <br> 

## 🚀 Ferramentas Utilizadas

* 🐳 Docker

* 🕊️ Flyway

* 📊 JaCoCo

* 📝 Log4j2

* 🔴 Lombok

* ☕️ Java 25

* 🧪 JUnit 5

* 🗺️ MapStruct

* 🃏 Jackson 3

* 🐘 PostgreSQL 18

* ⚙️ GitHub Actions

* 🟢 Spring Boot 4.1.1

* 🐘 Gradle 9.7.1 (Kotlin DSL)

* 📄 Springdoc OpenAPI (Swagger UI)

<br> 

## ⚙️ Pré-requisitos

* JDK 25 instalada (o projeto não declara resolver de toolchain, então o Gradle não baixa a JDK sozinho)

* PostgreSQL acessível para os perfis `dev`, `prod` e `test`

<br> 

## 🔐 Variáveis de Ambiente

Obrigatórias nos perfis `dev` e `prod` (usadas por `DataBaseConfig`):

| Variável | Descrição |
|---|---|
| `DATABASE_IP` | Host do PostgreSQL |
| `DATABASE_PORT` | Porta do PostgreSQL |
| `DATABASE_NAME` | Nome do banco |
| `DATABASE_USER` | Usuário do banco |
| `DATABASE_PASSWORD` | Senha do banco |

No perfil `test` as mesmas variáveis são lidas por `TestDataBaseConfig`, que assume
`localhost:5432/prisma` quando elas não estão definidas.

<br> 

## ▶️ Como Executar

```bash
# Ambiente de desenvolvimento (porta 9017)
./gradlew bootRun --args="--spring.profiles.active=dev"

# Ambiente de produção (porta 9027)
./gradlew bootRun --args="--spring.profiles.active=prod"
```

No IntelliJ IDEA, as configurações equivalentes estão em `.run/`.

A aplicação sobe sob o context path `/PrismaAPI`. Em desenvolvimento, a documentação
fica em `http://localhost:9017/PrismaAPI/swagger-ui.html`.

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

<br> 

## 🐳 Docker

```bash
# Sobe um PostgreSQL local na porta 5432
docker compose -f docker-compose-postgres.yml up -d

# Sobe a API em produção na porta 9027, lendo as variáveis de um .env ao lado
docker compose -f docker-compose-prismaapi.yml up -d
```

O `Dockerfile` constrói em dois estágios: `gradle:jdk25` gera o jar com
`gradle build -x test` e `eclipse-temurin:25-jre` executa `PrismaAPI.jar`.

<br> 

## 🤖 Integração Contínua

| Workflow | Gatilho | O que faz |
|---|---|---|
| `workflow.yml` | Pull Request para `main` | Sobe um PostgreSQL de serviço e roda `build jacocoTestReport` |
| `release.yml` | Push em `main` | Lê a versão do `build.gradle.kts` e publica a imagem no Docker Hub |

<br> 

## 📁 Estrutura

```
src/main/java/br/com/prismaapi
├── PrismaAPIApplication.java   # Classe de inicialização
├── config                      # CorsConfig, DataBaseConfig e JacksonConfig
├── controller                  # Um pacote por recurso: <Recurso>Controller + <Recurso>Docs
├── enums                       # Enums do domínio
├── exceptions                  # Exceções de negócio, dto e GlobalExceptionHandler
├── model                       # dto, entity e mapper (MapStruct)
├── repository                  # Repositórios Spring Data e Specifications
└── service                     # Regras de negócio e cálculos

src/main/resources
├── application.yaml            # Configuração por perfil (dev, prod, test)
├── banner.txt                  # Banner com versão e data do build
├── log4j2.xml                  # Configuração de logging
└── db/migration                # Migrations do Flyway

src/test/java/br/com/prismaapi
├── config                      # AbstractTest, AbstractControllerTest e TestDataBaseConfig
└── repository                  # Um pacote por repositório: <Recurso>RepositoryTest

src/test/resources
└── db/test                     # Massa de dados aplicada só no perfil test
```

<br> 
 
## 🖥️ Desenvolvedor

### 🔵 LinkedIn: [Gustavo Correa](https://www.linkedin.com/in/gustavo-chauar-correa-946168269/)
