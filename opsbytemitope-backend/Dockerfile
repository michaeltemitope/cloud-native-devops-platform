# =============================================================
# OpsByTemitope Backend – Dockerfile
# Written by the backend engineering team for development and
# local testing purposes.
#
# NOTE FOR PLATFORM/DEVOPS TEAM:
# This Dockerfile is intentionally straightforward. It is meant
# to prove the application builds and runs correctly inside a
# container. Production hardening (non-root user, distroless
# base, multi-arch builds, image signing, etc.) is expected to
# be applied by the platform team as part of the deployment
# pipeline strategy.
# =============================================================

# ── Stage 1: Build ────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copy Maven wrapper and POM first to benefit from layer caching.
# The dependency layer only rebuilds when pom.xml changes.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build the fat JAR, skipping tests.
# Tests are expected to run in CI, not during image build.
COPY src src

RUN ./mvnw package -DskipTests -B && \
    mkdir -p target/extracted && \
    java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# ── Stage 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="OpsByTemitope Engineering <engineering@opsbytemitope.com>"
LABEL org.opencontainers.image.title="opsbytemitope-backend"
LABEL org.opencontainers.image.description="OpsByTemitope cloud-native task management API"

WORKDIR /app

# Copy layered JAR for faster startup via Spring Boot layertools
COPY --from=builder /workspace/target/extracted/dependencies/ ./
COPY --from=builder /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/target/extracted/application/ ./

EXPOSE 8080

# Spring Boot app entry point via layered JAR loader
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "org.springframework.boot.loader.launch.JarLauncher"]
