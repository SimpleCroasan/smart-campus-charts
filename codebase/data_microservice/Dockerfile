# syntax=docker/dockerfile:1.4


##########################
# Stage 1: Build
##########################
FROM maven:3.9.9-eclipse-temurin-21-jammy AS builder
WORKDIR /build


# 1. Copiar solo los POMs de todos los módulos
COPY pom.xml ./
COPY application/pom.xml application/
COPY domain/pom.xml domain/
COPY persistence/pom.xml persistence/
COPY service/pom.xml service/


# 2. Descargar dependencias en cache
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp dependency:go-offline -pl application -am


# 3. Copiar el código fuente completo
COPY . .


# 4. Compilar solo el módulo application (y los necesarios)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp clean package -DskipTests -pl application -am


##########################
# Stage 2: Runtime
##########################
FROM eclipse-temurin:21-jre-jammy


# Instalar ffmpeg (mínimo posible, sin paquetes recomendados)
RUN apt-get update \
 && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends ffmpeg \
 && rm -rf /var/lib/apt/lists/*


WORKDIR /app
COPY --from=builder /build/application/target/*.jar /app/app.jar


EXPOSE 8080


ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]

