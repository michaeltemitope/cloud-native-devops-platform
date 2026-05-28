# OpsByTemitope Frontend

A cloud-native task and project management platform frontend built with **React 18**, **Vite**, and **Axios**. Designed as a realistic SaaS-style web application suitable for DevOps deployment, containerisation, and Kubernetes orchestration.

This is the **frontend layer only** — it expects a running backend API. See [Environment Variables](#environment-variables) to configure the API base URL.

---

## Table of Contents

- [Application Overview](#application-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Environment Variables](#environment-variables)
- [Local Development](#local-development)
- [Running Tests](#running-tests)
- [Building for Production](#building-for-production)
- [Running with Docker](#running-with-docker)
- [Routing & Authentication](#routing--authentication)
- [API Integration](#api-integration)
- [Code Quality](#code-quality)

---

## Application Overview

OpsByTemitope provides teams with a clean interface to manage projects and tasks. Features include:

- JWT-based authentication (register / login / logout)
- Protected routes requiring authentication
- Dashboard with KPI cards, recent activity, and open tasks
- Project management (create, view, update, delete)
- Task management with status and priority filters
- User profile management and password change
- Responsive sidebar-based layout

---

## Tech Stack

| Technology              | Version  | Purpose                     |
|-------------------------|----------|-----------------------------|
| React                   | 18.2     | UI framework                |
| Vite                    | 5.x      | Build tool and dev server   |
| React Router DOM        | 6.x      | Client-side routing         |
| Axios                   | 1.6.x    | HTTP client                 |
| Vitest                  | 1.3.x    | Unit test runner            |
| Testing Library / React | 14.x     | Component testing utilities |
| ESLint                  | 8.x      | Linting                     |
| Prettier                | 3.x      | Code formatting             |
| nginx                   | 1.25     | Production static file server |

---

## Project Structure

```
src/
├── assets/styles/global.css       # CSS variables and global reset
├── components/
│   ├── common/                    # Reusable UI primitives
│   │   ├── Alert                  # Error/success/warning banners
│   │   ├── Badge                  # Status/priority labels
│   │   ├── Button                 # Primary, ghost, danger, secondary variants
│   │   ├── Card                   # Surface container
│   │   ├── Input                  # Labelled input with error state
│   │   ├── Modal                  # Accessible dialog overlay
│   │   ├── Select                 # Styled select element
│   │   └── Spinner                # Loading indicator
│   ├── dashboard/
│   │   ├── ActivityFeed           # Recent activity event list
│   │   └── StatCard               # KPI summary card
│   ├── layout/
│   │   ├── Header                 # Top bar with page title
│   │   └── Sidebar                # Navigation sidebar with user info
│   ├── projects/
│   │   ├── ProjectCard            # Card view of a single project
│   │   └── ProjectForm            # Create/edit project modal form
│   └── tasks/
│       ├── TaskForm               # Create/edit task modal form
│       └── TaskTable              # Tabular task listing with inline status
├── context/
│   └── AuthContext.jsx            # JWT auth state, login/logout/register
├── hooks/
│   ├── useApi.js                  # Generic async hook with loading/error state
│   ├── useProjects.js             # Fetch and mutate project list
│   └── useTasks.js                # Fetch and mutate task list
├── layouts/
│   ├── AppLayout.jsx              # Authenticated shell: sidebar + header + outlet
│   └── AuthLayout.jsx             # Centered card layout for login/register
├── pages/
│   ├── DashboardPage.jsx          # Summary stats + activity feed
│   ├── LoginPage.jsx              # Email/password login
│   ├── RegisterPage.jsx           # New account registration
│   ├── NotFoundPage.jsx           # 404 fallback
│   ├── ProfilePage.jsx            # Update profile + change password
│   ├── ProjectDetailPage.jsx      # Project detail + task management
│   ├── ProjectsPage.jsx           # Project listing
│   └── TasksPage.jsx              # Global task list with search and filter
├── routes/AppRoutes.jsx           # Route definitions with protected/guest guards
├── services/
│   ├── api.js                     # Axios instance with JWT request interceptor
│   ├── authService.js             # /auth/* endpoints
│   ├── dashboardService.js        # /dashboard endpoints
│   ├── projectService.js          # /projects/* endpoints
│   └── taskService.js             # /tasks/* endpoints
└── utils/helpers.js               # formatDate, timeAgo, getInitials, getErrorMessage

tests/
├── components/                    # Unit tests for UI components and utilities
├── pages/                         # Rendering and interaction tests for pages
└── setup.js                       # @testing-library/jest-dom setup
```

---

## Prerequisites

| Tool    | Minimum Version | Notes                          |
|---------|-----------------|--------------------------------|
| Node.js | 18.x            | LTS recommended (20.x in Docker) |
| npm     | 9.x+            | Bundled with Node               |
| Docker  | 24+             | For containerised build/run     |

---

## Environment Variables

Copy `.env.example` to `.env.local` and set values for your environment:

```bash
cp .env.example .env.local
```

| Variable             | Required | Default                              | Description                              |
|----------------------|----------|--------------------------------------|------------------------------------------|
| `VITE_API_BASE_URL`  | No       | `http://localhost:8080/api/v1`       | Backend API base URL                     |
| `VITE_APP_NAME`      | No       | `OpsByTemitope`                      | Application name used in page titles     |

> **Docker builds**: The `VITE_API_BASE_URL` is baked into the static build at build time by Vite. Pass it as a build argument if you need a custom URL in the Docker image.

---

## Local Development

```bash
# Install dependencies
npm install

# Start development server (hot reload at http://localhost:3000)
npm run dev
```

The dev server proxies nothing by default. Ensure the backend API is running and set `VITE_API_BASE_URL` in `.env.local` to match.

---

## Running Tests

```bash
# Run all tests once
npm test

# Watch mode (reruns on file change)
npm run test:watch

# With coverage report
npm run test:coverage
```

Tests use **Vitest** + **Testing Library**. No external services are required.

### What is tested

- **Component tests**: `Alert`, `Badge`, `Button`, `Input`, `Modal`, `helpers.js`
- **Page tests**: `LoginPage` (render, validation, API integration, error handling), `NotFoundPage`

---

## Building for Production

```bash
npm run build
# Output: dist/
```

Preview the production build locally:

```bash
npm run preview
```

---

## Running with Docker

### Build the image

```bash
docker build -t opsbytemitope-frontend:latest .
```

To override the API URL at build time:

```bash
docker build \
  --build-arg VITE_API_BASE_URL=https://api.yourapp.com/api/v1 \
  -t opsbytemitope-frontend:latest .
```

> **Note**: You must also update the Dockerfile `ARG`/`ENV` declarations if passing build args.
> For dynamic runtime configuration, consider using a small nginx config snippet that rewrites `window.ENV` from env vars at container start — this is left for the DevOps team to decide on as a platform pattern.

### Run the container

```bash
docker run -d -p 80:80 opsbytemitope-frontend:latest
```

The frontend is served by nginx on port 80 with SPA fallback routing enabled.

> **Note to DevOps team**: The Dockerfile is intentionally straightforward. Production hardening (non-root nginx user, security headers, HTTPS termination at ingress level, etc.) is expected to be applied by the platform team.

---

## Routing & Authentication

| Route                | Auth required | Description                   |
|----------------------|---------------|-------------------------------|
| `/login`             | No (guest)    | Login page                    |
| `/register`          | No (guest)    | Register page                 |
| `/dashboard`         | Yes           | Dashboard with metrics        |
| `/projects`          | Yes           | Project list                  |
| `/projects/:id`      | Yes           | Project detail + tasks        |
| `/tasks`             | Yes           | Global task list              |
| `/profile`           | Yes           | User profile management       |
| `/`                  | —             | Redirects to `/dashboard`     |
| `*`                  | —             | 404 Not Found page            |

- Authenticated users visiting guest routes (`/login`, `/register`) are redirected to `/dashboard`.
- Unauthenticated users visiting protected routes are redirected to `/login`.
- JWT is stored in `localStorage` and attached to all API requests via an Axios interceptor.
- A `401` response from the API automatically clears the token and redirects to `/login`.

---

## API Integration

All API calls go through the Axios instance in `src/services/api.js`, which:

- Sets `baseURL` from `VITE_API_BASE_URL`
- Attaches the JWT `Authorization: Bearer <token>` header automatically
- Handles `401` responses by clearing local state and redirecting to login
- Sets a 15-second request timeout

The backend wraps all responses in `{ success, message, data }`. Service functions unwrap to `response.data.data` before returning to components.

---

## Code Quality

```bash
# Lint
npm run lint

# Auto-fix lint errors
npm run lint:fix

# Check formatting
npm run format:check

# Auto-format
npm run format
```

- ESLint config: `.eslintrc.cjs`
- Prettier config: `.prettierrc`
