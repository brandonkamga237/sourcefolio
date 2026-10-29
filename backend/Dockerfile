# syntax=docker/dockerfile:1

# ── Stage 1: build ────────────────────────────────────────────────────────────
# --mount=type=cache keeps /root/.m2 across builds through BuildKit, so a pom.xml
# change only downloads the new dependencies.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
# The Playwright image ships Chromium and every system library it needs, so screenshots
# work without a download at startup. Its version must match the
# com.microsoft.playwright dependency in pom.xml: bump both together.
FROM mcr.microsoft.com/playwright/java:v1.64.0-noble AS runtime
WORKDIR /app
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
# The image runs as root and ships JDK 25, which runs our Java 21 bytecode. The browsers
# live in the world-readable /ms-playwright, so an unprivileged user can launch them.
RUN useradd --create-home --uid 10001 app \
 && mkdir -p /var/log/sourcefolio && chown app:app /var/log/sourcefolio
COPY --from=build --chown=app:app /app/target/*.jar app.jar
USER app
EXPOSE 8080

FROM runtime AS dev
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]

FROM runtime AS prod
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "app.jar", "--spring.profiles.active=prod"]
