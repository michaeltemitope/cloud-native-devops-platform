# =============================================================
# OpsByTemitope Frontend – Dockerfile
# Written by the frontend engineering team.
#
# NOTE FOR PLATFORM/DEVOPS TEAM:
# This Dockerfile is intentionally straightforward. It is meant
# to prove the application builds and runs correctly inside a
# container. Production hardening (non-root nginx user, custom
# security headers, multi-arch builds) is expected to be applied
# by the platform team as part of the deployment pipeline.
#
# The VITE_API_BASE_URL is baked in at build time by Vite.
# If you need runtime-configurable API URLs, consider an nginx
# entrypoint script that injects window.ENV from environment
# variables — this is a platform-level concern.
# =============================================================

# ── Stage 1: Build ────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Install dependencies first for better layer caching.
# This layer only rebuilds when package*.json changes.
COPY package*.json ./
RUN npm ci

# Copy source and build static assets.
COPY . .

# Accept API base URL as a build argument (optional).
# If not provided, falls back to the VITE_API_BASE_URL in .env / defaults in api.js.
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN npm run build

# ── Stage 2: Serve ────────────────────────────────────────────
FROM nginx:1.25-alpine

LABEL maintainer="OpsByTemitope Engineering <engineering@opsbytemitope.com>"
LABEL org.opencontainers.image.title="opsbytemitope-frontend"
LABEL org.opencontainers.image.description="OpsByTemitope cloud-native project management frontend"

# Replace default nginx config with our SPA-aware config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built static assets from the builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
