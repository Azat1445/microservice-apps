# Multi-stage build
FROM gradle:9.2.1-jdk17 AS build

WORKDIR /workspace

# Копируем корневые файлы сборки (для монорепо)
COPY settings.gradle ./
COPY build.gradle ./
COPY gradlew ./
COPY gradle ./gradle

# Копируем весь NotificationService
COPY NotificationService ./NotificationService

# Собираем только NotificationService
RUN ./gradlew :NotificationService:build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем JAR из builder stage
COPY --from=build /workspace/NotificationService/build/libs/*.jar app.jar

# Expose порты
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Запуск
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
