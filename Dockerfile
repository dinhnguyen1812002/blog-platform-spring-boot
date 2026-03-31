# ============================
# Stage 1: Build
# ============================
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copy Gradle wrapper (cache tốt hơn)
COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

# Copy build config
COPY build.gradle settings.gradle ./

# Cache dependencies
RUN ./gradlew dependencies --no-daemon || true

# Copy source
COPY src src

# Build jar
RUN ./gradlew clean bootJar --no-daemon

# ============================
# Stage 2: Runtime
# ============================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring springuser

# Create directories
RUN mkdir -p /app/logs /app/uploads && \
    chown -R springuser:spring /app

# Copy jar
COPY --from=builder /app/build/libs/*.jar app.jar

# Set ownership
RUN chown springuser:spring app.jar

# Switch user
USER springuser

# Activate prod profile
ENV SPRING_PROFILES_ACTIVE=prod

# Volume for uploads
VOLUME ["/app/uploads"]

# Expose port
EXPOSE 8080

# JVM tuning
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
