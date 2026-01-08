# ============================
# Stage 1: Build the Spring Boot app
# ============================
FROM eclipse-temurin:25-jdk AS builder
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
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Create non-root user first
RUN addgroup -S spring && adduser -S springuser -G spring

# Create logs directory and set ownership
RUN mkdir -p /app/logs && chown -R springuser:spring /app/logs

# Copy jar file from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership of app.jar
RUN chown springuser:spring app.jar

# Switch to non-root user
USER springuser

# Expose port
EXPOSE 8080

# Run the app with JVM optimizations
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]