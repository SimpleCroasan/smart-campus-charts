
FROM maven:3.9.9-eclipse-temurin-21-jammy AS builder
WORKDIR /build
COPY pom.xml ./
COPY application/pom.xml application/
COPY domain/pom.xml domain/
COPY persistence/pom.xml persistence/
COPY service/pom.xml service/
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp dependency:go-offline -pl application -am
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp clean package -DskipTests -pl application -am

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update \
 && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends ffmpeg \
 && rm -rf /var/lib/apt/lists/*

ARG OTEL_AGENT_VERSION=2.11.0
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar \
    /app/opentelemetry-javaagent.jar

WORKDIR /app
COPY --from=builder /build/application/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-javaagent:/app/opentelemetry-javaagent.jar", \
    "-jar", "/app/app.jar"]