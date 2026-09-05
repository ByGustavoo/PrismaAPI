<div align="center"> <br> 
  <img align="center" alt="guru-java" height="150" width="150" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/spring/spring-original.svg" />
</div> 

<br> 

<div align="center">
  API REST do PrismaWeb, construída em Spring Boot. Concentra as regras de negócio, a persistência e os endpoints consumidos pela aplicação web.
</div> 

 <br> 

## 🚀 Ferramentas Utilizadas

* 🕊️ Flyway

* 📊 JaCoCo

* 📝 Log4j2

* 🔴 Lombok

* ☕️ Java 21

* 🗺️ MapStruct

* 🐘 PostgreSQL

* 🟢 Spring Boot 4.1.1

* 🐘 Gradle 9.7.1 (Kotlin DSL)

* 📄 Springdoc OpenAPI (Swagger UI)

<br> 

## ⚙️ Pré-requisitos

* JDK 21 (o Gradle resolve a toolchain automaticamente)

* PostgreSQL acessível para os perfis `dev` e `prod`

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
# Testes (gera o relatório JaCoCo em build/reports/jacoco)
./gradlew test

# Build sem testes
./gradlew clean build -x test
```

<br> 

## 📁 Estrutura

```
src/main/java/br/com/software
├── PrismaAPIApplication.java   # Classe de inicialização
└── config                      # Configurações Spring (ex.: DataBaseConfig)

src/main/resources
├── application.yaml            # Configuração por perfil (dev, prod, test)
└── log4j2.xml                  # Configuração de logging
```

<br> 
 
## 🖥️ Desenvolvedor

### 🔵 LinkedIn: [Gustavo Correa](https://www.linkedin.com/in/gustavo-chauar-correa-946168269/)
