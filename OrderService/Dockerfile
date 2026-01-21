# Multi-stage build
FROM gradle:9.2.1-jdk17 AS build

WORKDIR /workspace

# Копируем корневые файлы сборки (для монорепо)
COPY settings.gradle ./
COPY build.gradle ./
COPY gradlew ./
COPY gradle ./gradle

# Копируем весь OrderService
COPY OrderService ./OrderService

# Собираем только OrderService
RUN ./gradlew :OrderService:build -x test --no-daemon

# Этап 2: Создаем финальный образ
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем JAR из build stage
COPY --from=build /workspace/OrderService/build/libs/*.jar app.jar

# Expose ports (8081 REST; gRPC client подключается к inventory-service:9090)
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
