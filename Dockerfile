# Dockerfile
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy source code và gradle wrapper
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Build ứng dụng
RUN ./gradlew clean bootJar --no-daemon

# Image chạy thực tế
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
