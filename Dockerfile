FROM gradle:jdk25 AS builder

WORKDIR /app

COPY . .

RUN gradle build -x test --no-daemon

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar PrismaAPI.jar

ENTRYPOINT ["java", "-jar", "PrismaAPI.jar"]