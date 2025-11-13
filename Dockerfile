# ============================
# Stage 1: Build the Spring Boot app
# ============================
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy Gradle wrapper and project files
COPY gradlew .
RUN chmod +x gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

# Build the application (no daemon for CI/CD optimization)
RUN ./gradlew clean bootJar --no-daemon

# ============================
# Stage 2: Run the built jar
# ============================
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy jar file from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Use a non-root user for better security
RUN useradd -m springuser
USER springuser

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
