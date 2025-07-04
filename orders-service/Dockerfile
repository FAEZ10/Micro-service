# Multi-stage build pour optimiser la taille
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copier pom.xml et télécharger les dépendances (cache Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier et compiler le code source
COPY src ./src
RUN mvn clean package -DskipTests

# Image runtime optimisée
FROM openjdk:17-jdk-slim

WORKDIR /app

# Sécurité : utilisateur non-root
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -s /bin/bash -m appuser

# Installer curl pour health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copier le JAR compilé
COPY --from=build /app/target/*.jar app.jar

# Créer dossier logs avec permissions
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

# Passer à l'utilisateur non-root
USER appuser

# Port du service
EXPOSE 8083

# Variables d'environnement
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/swagger-ui.html || exit 1

# Point d'entrée
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
