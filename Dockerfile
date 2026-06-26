# ============================================================
# TeleOps AI — Dockerfile
# Multi-stage build: smaller final image
# ============================================================

# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first — Docker caches dependencies separately
# If pom.xml doesn't change, dependencies are not re-downloaded
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────────────────────
# Stage 2: Run the application
# eclipse-temurin:21-jre is smaller than the full JDK image
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create non-root user for security
RUN groupadd -r teleops && useradd -r -g teleops teleops

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership
RUN chown teleops:teleops app.jar

USER teleops

# Expose the application port
EXPOSE 8080

# JVM tuning for containers:
#   -XX:+UseContainerSupport = respect container memory limits
#   -XX:MaxRAMPercentage = use 75% of container RAM for heap
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]