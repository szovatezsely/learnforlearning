# syntax=docker/dockerfile:1

# ---- Build stage: compile and package the executable boot jar ----------------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Copy the Gradle wrapper and build scripts first so dependency resolution is
# cached as its own layer and only re-runs when these files change.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x ./gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Now copy the sources and build (bootJar skips tests for a fast image build).
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar

# ---- Runtime stage: small JRE image, non-root user ---------------------------
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# Run as an unprivileged user.
RUN useradd -r -u 1001 spring
USER spring

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
