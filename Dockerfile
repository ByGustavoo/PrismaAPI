FROM --platform=$BUILDPLATFORM gradle:jdk25 AS builder

ARG VERSION=

WORKDIR /app

COPY . .

RUN gradle build -x test --no-daemon ${VERSION:+-Pversao=$VERSION}

FROM eclipse-temurin:25-jre

ARG VERSION=
ARG REVISION=unknown
ARG BUILD_DATE=

LABEL org.opencontainers.image.title="Prisma API" \
      org.opencontainers.image.description="API do Prisma, aplicação de finanças pessoais" \
      org.opencontainers.image.source="https://github.com/ByGustavoo/PrismaAPI" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${REVISION}" \
      org.opencontainers.image.created="${BUILD_DATE}"

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar PrismaAPI.jar

ENTRYPOINT ["java", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8", "-jar", "PrismaAPI.jar"]
