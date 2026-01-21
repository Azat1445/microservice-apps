# Multi-stage build
FROM gradle:9.2.1-jdk17 AS builder

WORKDIR /workspace

# Копируем корневые файлы сборки (для монорепо)
COPY settings.gradle ./
COPY build.gradle ./
COPY gradlew ./
COPY gradle ./gradle

# Копируем весь InventoryService
COPY InventoryService ./InventoryService

# Собираем только InventoryService
RUN ./gradlew :InventoryService:build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем JAR из builder stage
COPY --from=builder /workspace/InventoryService/build/libs/*.jar app.jar

# Expose ports
EXPOSE 8082 9090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Run
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
