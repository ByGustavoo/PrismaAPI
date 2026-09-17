FROM gradle:jdk25 AS builder

WORKDIR /app

COPY . .

RUN gradle build -x test --no-daemon

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar PrismaAPI.jar

ENTRYPOINT ["java", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8", "-jar", "PrismaAPI.jar"]