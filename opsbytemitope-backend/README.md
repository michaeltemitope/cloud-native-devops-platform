# OpsByTemitope Backend

A production-grade, cloud-native REST API for the **OpsByTemitope** task and project management platform. Built with Java 21, Spring Boot 3, PostgreSQL, and Redis.

This service is the backend engineering deliverable intended for handoff to a DevOps/platform engineering team for containerization, orchestration, and deployment.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Local Development Setup](#local-development-setup)
- [Environment Variables](#environment-variables)
- [Running the Application](#running-the-application)
- [Running with Docker](#running-with-docker)
- [Database Migrations](#database-migrations)
- [API Documentation](#api-documentation)
- [Authentication](#authentication)
- [Testing](#testing)
- [Configuration Profiles](#configuration-profiles)
- [Health & Actuator Endpoints](#health--actuator-endpoints)
- [Code Quality](#code-quality)
- [Seed Data](#seed-data)

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────┐
│                    React Frontend (separate)                  │
└──────────────────────┬───────────────────────────────────────┘
                       │ HTTP / REST (JSON)
┌──────────────────────▼───────────────────────────────────────┐
│                   Spring Boot REST API                        │
│                                                               │
│  Controllers → Services → Repositories → Entities            │
│                    │                                          │
│              Security Layer (JWT)                             │
│              Global Exception Handler                         │
│              Swagger / OpenAPI                                │
└──────────┬─────────────────────────────┬─────────────────────┘
           │                             │
    ┌──────▼──────┐               ┌──────▼──────┐
    │ PostgreSQL  │               │    Redis     │
    │  (primary)  │               │   (cache)    │
    └─────────────┘               └─────────────┘
```

The application follows a **layered architecture**:

| Layer        | Package                        | Responsibility                             |
|--------------|--------------------------------|--------------------------------------------|
| Controller   | `com.opsbytemitope.controller` | HTTP request/response handling             |
| Service      | `com.opsbytemitope.service`    | Business logic                             |
| Repository   | `com.opsbytemitope.repository` | Data access (Spring Data JPA)              |
| Entity       | `com.opsbytemitope.entity`     | JPA-mapped domain objects                  |
| DTO          | `com.opsbytemitope.dto`        | Request/response data transfer objects     |
| Security     | `com.opsbytemitope.security`   | JWT filter, UserDetailsService             |
| Config       | `com.opsbytemitope.config`     | Spring beans (Security, Redis, OpenAPI)    |
| Exception    | `com.opsbytemitope.exception`  | Global handler + custom exceptions         |
| Util         | `com.opsbytemitope.util`       | JWT utilities                              |

---

## Tech Stack

| Component       | Technology          | Version |
|-----------------|---------------------|---------|
| Language        | Java                | 21      |
| Framework       | Spring Boot         | 3.3.x   |
| Build tool      | Maven               | 3.9+    |
| Database        | PostgreSQL          | 16      |
| Cache           | Redis               | 7       |
| Migrations      | Flyway              | 10.x    |
| Auth            | JWT (JJWT)          | 0.12.x  |
| API Docs        | SpringDoc OpenAPI 3 | 2.5.x   |
| Observability   | Spring Actuator + Prometheus Micrometer | — |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/opsbytemitope/
│   │   ├── OpsByTemitopeApplication.java   # Entry point
│   │   ├── config/                          # Security, Redis, OpenAPI config
│   │   ├── controller/                      # REST controllers
│   │   ├── dto/request/                     # Incoming request DTOs
│   │   ├── dto/response/                    # Outgoing response DTOs
│   │   ├── entity/                          # JPA entities + enums
│   │   ├── exception/                       # Custom exceptions + global handler
│   │   ├── repository/                      # Spring Data JPA repositories
│   │   ├── security/                        # JWT filter + UserDetailsService
│   │   ├── service/                         # Service interfaces
│   │   │   └── impl/                        # Service implementations
│   │   └── util/                            # JwtUtil
│   └── resources/
│       ├── application.yml                  # Shared base config
│       ├── application-dev.yml              # Development profile
│       ├── application-prod.yml             # Production profile
│       └── db/migration/                    # Flyway SQL migrations
└── test/
    ├── java/com/opsbytemitope/
    │   ├── config/TestSecurityConfig.java
    │   ├── controller/                      # MVC slice tests
    │   ├── integration/                     # Full-stack integration tests (Testcontainers)
    │   └── service/                         # Unit tests
    └── resources/application-test.yml
```

---

## Prerequisites

| Tool       | Minimum Version | Notes                         |
|------------|-----------------|-------------------------------|
| Java JDK   | 21              | Eclipse Temurin recommended   |
| Maven      | 3.9+            | Or use the bundled `./mvnw`   |
| PostgreSQL | 16              | Or run via Docker             |
| Redis      | 7               | Or run via Docker             |
| Docker     | 24+             | For containerised development |

---

## Local Development Setup

### 1. Clone and configure environment

```bash
cp .env.example .env
# Edit .env and fill in your local DB and Redis credentials
```

### 2. Start infrastructure (PostgreSQL + Redis)

```bash
# Quick start with Docker (no docker-compose.yml needed):
docker run -d --name opsbytemitope-pg \
  -e POSTGRES_DB=opsbytemitope_dev \
  -e POSTGRES_USER=opsbytemitope \
  -e POSTGRES_PASSWORD=change_me_local \
  -p 5432:5432 postgres:16-alpine

docker run -d --name opsbytemitope-redis \
  -p 6379:6379 redis:7-alpine
```

### 3. Run the application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API starts on `http://localhost:8080`.

---

## Environment Variables

All configurable values are documented in `.env.example`. Key variables:

| Variable                 | Required       | Default                          | Description                          |
|--------------------------|----------------|----------------------------------|--------------------------------------|
| `SPRING_PROFILES_ACTIVE` | No             | `dev`                            | Active Spring profile                |
| `SERVER_PORT`            | No             | `8080`                           | HTTP server port                     |
| `DB_HOST`                | Yes (prod)     | `localhost`                      | PostgreSQL host                      |
| `DB_PORT`                | No             | `5432`                           | PostgreSQL port                      |
| `DB_NAME`                | Yes (prod)     | `opsbytemitope_dev`              | Database name                        |
| `DB_USERNAME`            | Yes (prod)     | `opsbytemitope`                  | Database username                    |
| `DB_PASSWORD`            | Yes            | —                                | Database password                    |
| `REDIS_HOST`             | Yes (prod)     | `localhost`                      | Redis host                           |
| `REDIS_PORT`             | No             | `6379`                           | Redis port                           |
| `REDIS_PASSWORD`         | No             | (empty)                          | Redis auth password                  |
| `JWT_SECRET`             | **Yes**        | (dev placeholder)                | 256-bit JWT signing secret           |
| `JWT_EXPIRATION_MS`      | No             | `86400000` (24h)                 | Token lifetime in milliseconds       |
| `APP_BASE_URL`           | No             | `http://localhost:8080`          | Used in Swagger UI server list       |

> **Security**: In production, `JWT_SECRET` must be a cryptographically random 256-bit value.
> Generate one with: `openssl rand -hex 32`

---

## Running the Application

### Local (dev profile)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### With explicit environment variables

```bash
DB_HOST=myhost DB_PASSWORD=secret ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## Running with Docker

### Build the image

```bash
docker build -t opsbytemitope-backend:latest .
```

### Run the container

```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=<postgres-host> \
  -e DB_NAME=opsbytemitope \
  -e DB_USERNAME=opsbytemitope \
  -e DB_PASSWORD=<secret> \
  -e REDIS_HOST=<redis-host> \
  -e REDIS_PASSWORD=<redis-secret> \
  -e JWT_SECRET=<256-bit-secret> \
  opsbytemitope-backend:latest
```

> **Note to DevOps team**: The Dockerfile is intentionally straightforward — it proves the application builds and runs correctly in a container. Production hardening (non-root user, distroless base, resource limits, image signing) is expected to be applied by the platform team as part of the deployment pipeline strategy.

---

## Database Migrations

Flyway is configured to run automatically on startup.

| File                          | Description                                      |
|-------------------------------|--------------------------------------------------|
| `V1__init_schema.sql`         | Creates all tables: users, projects, tasks, activity_events |
| `V2__seed_data.sql`           | Inserts demo data for development                |

- **`ddl-auto: validate`** — Hibernate validates schema against migrations (does not auto-create/drop).
- Flyway baseline is enabled, so the app can be run against a pre-existing database.
- Migrations live in `src/main/resources/db/migration/`.

---

## API Documentation

Swagger UI is available when the application is running:

| URL                                      | Description           |
|------------------------------------------|-----------------------|
| `http://localhost:8080/swagger-ui.html`  | Interactive Swagger UI |
| `http://localhost:8080/v3/api-docs`      | Raw OpenAPI 3.0 JSON  |

Authentication is required for most endpoints. Use the "Authorize" button in Swagger UI and enter your JWT token in the format: `Bearer <token>`.

### Core Endpoints

| Method   | Path                        | Auth | Description                   |
|----------|-----------------------------|------|-------------------------------|
| `POST`   | `/api/v1/auth/register`     | No   | Register new user             |
| `POST`   | `/api/v1/auth/login`        | No   | Login, receive JWT            |
| `GET`    | `/api/v1/users/me`          | Yes  | Get authenticated user        |
| `PUT`    | `/api/v1/users/me`          | Yes  | Update profile                |
| `PUT`    | `/api/v1/users/me/password` | Yes  | Change password               |
| `GET`    | `/api/v1/projects`          | Yes  | List user's projects (paged)  |
| `POST`   | `/api/v1/projects`          | Yes  | Create project                |
| `GET`    | `/api/v1/projects/{id}`     | Yes  | Get project by ID             |
| `PUT`    | `/api/v1/projects/{id}`     | Yes  | Update project                |
| `DELETE` | `/api/v1/projects/{id}`     | Yes  | Delete project                |
| `GET`    | `/api/v1/tasks`             | Yes  | List tasks (scoped/global)    |
| `POST`   | `/api/v1/tasks`             | Yes  | Create task                   |
| `GET`    | `/api/v1/tasks/{id}`        | Yes  | Get task by ID                |
| `PUT`    | `/api/v1/tasks/{id}`        | Yes  | Update task                   |
| `PATCH`  | `/api/v1/tasks/{id}/status` | Yes  | Update task status            |
| `DELETE` | `/api/v1/tasks/{id}`        | Yes  | Delete task                   |
| `GET`    | `/api/v1/dashboard`         | Yes  | Dashboard metrics             |
| `GET`    | `/api/v1/activity`          | Yes  | Recent activity events        |

---

## Authentication

The API uses stateless JWT authentication:

1. Register via `POST /api/v1/auth/register` or login via `POST /api/v1/auth/login`.
2. Both endpoints return an `accessToken` (Bearer JWT).
3. Include the token in subsequent requests: `Authorization: Bearer <token>`.
4. Tokens expire after 24 hours by default (`JWT_EXPIRATION_MS`).
5. On expiry or invalid token, the API returns `401 Unauthorized`.

---

## Testing

### Unit tests (fast, no infrastructure)

```bash
./mvnw test
```

Runs all `*Test.java` files. Uses H2 in-memory database and mocked Redis. No external services required.

### Integration tests (requires Docker for Testcontainers)

```bash
./mvnw verify
```

Runs `*IntegrationTest.java` files via Maven Failsafe. Spins up a real PostgreSQL container via Testcontainers. Docker must be running.

### Test coverage

```bash
./mvnw verify jacoco:report
# Report at: target/site/jacoco/index.html
```

### Test profiles

| Profile  | Database           | Redis       | Purpose                    |
|----------|--------------------|-------------|----------------------------|
| `test`   | H2 (in-memory)     | Disabled    | Unit / slice tests         |
| (IT)     | Testcontainers PG  | Disabled    | Full-stack integration     |

---

## Configuration Profiles

| Profile | File                    | Use case                          |
|---------|-------------------------|-----------------------------------|
| `dev`   | `application-dev.yml`   | Local development (verbose logs)  |
| `prod`  | `application-prod.yml`  | Production (structured JSON logs) |
| `test`  | `application-test.yml`  | Automated testing (H2 + no Redis) |

Set via: `SPRING_PROFILES_ACTIVE=prod` or `-Dspring-boot.run.profiles=prod`.

---

## Health & Actuator Endpoints

Spring Boot Actuator is enabled. All endpoints are publicly accessible (no auth required) to support Kubernetes health probes.

| Endpoint                           | Description                              |
|------------------------------------|------------------------------------------|
| `GET /actuator/health`             | Overall application health               |
| `GET /actuator/health/liveness`    | Kubernetes liveness probe                |
| `GET /actuator/health/readiness`   | Kubernetes readiness probe (DB + Redis)  |
| `GET /actuator/info`               | Application metadata (name, version)     |
| `GET /actuator/metrics`            | Application metrics                      |
| `GET /actuator/prometheus`         | Prometheus-format metrics scrape target  |

**Production exposure**: Only `health`, `info`, `metrics`, `prometheus` are exposed in the `prod` profile. All endpoints are exposed in `dev`.

---

## Code Quality

```bash
# Run Checkstyle validation
./mvnw checkstyle:check

# Checkstyle config: checkstyle/checkstyle.xml
```

Checkstyle runs automatically during the `validate` Maven phase.

---

## Seed Data

`V2__seed_data.sql` inserts the following demo data in the `dev` environment:

- **2 users**: `admin@opsbytemitope.com` / `user@opsbytemitope.com`
  - Default password for both: `Password123!` (BCrypt-hashed)
- **2 projects** owned by the admin user
- **4 tasks** across those projects with varied statuses and priorities
- **4 activity events**

> Seed data only applies when Flyway runs V2. In production, use `SPRING_PROFILES_ACTIVE=prod` with a clean database to skip seed data by not applying V2 (or remove V2 from your migration location).
