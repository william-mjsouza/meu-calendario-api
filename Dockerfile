# Multi-stage build: primeiro compila o JAR, depois roda numa imagem enxuta

# --- Stage 1: build ---
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
# Copia só o pom primeiro para cache das dependências
COPY pom.xml .
RUN mvn -B dependency:go-offline
# Agora copia o código-fonte e empacota
COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Stage 2: runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
# O Render injeta a PORT dinamicamente; o Spring já lê ${PORT} do application.properties
ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
