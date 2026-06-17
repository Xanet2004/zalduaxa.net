# zalduaxa.net

A personal portfolio/project platform. Built with Spring Boot, React, PostgreSQL, and Docker.

## Current stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3, Java 21, PostgreSQL, Flyway, Spring Security JWT, BCrypt + pepper |
| Frontend | React, TypeScript, Vite, nginx |
| Observability | Prometheus, Grafana, Loki, Alloy, Uptime Kuma |
| Analytics | Matomo + custom Prometheus exporter |
| Quality | SonarQube, JaCoCo, Playwright, axe-core, Lighthouse |
| Infrastructure | Docker Compose, env-file split, local storage volume |

## Quick start

```bash
cp .env.example .env
cp .env.passwords.example .env.passwords
docker compose up -d --build
```

This starts the full local stack: frontend, backend API, PostgreSQL, SonarQube, Homepage, Prometheus, Grafana, Loki, Alloy, Uptime Kuma, Matomo, and the frontend quality runner.

### Access points

| Service | URL | Purpose |
|---------|-----|---------|
| Frontend | `http://localhost:5173` | React application |
| Backend health | `http://localhost:8080/actuator/health` | API health check |
| Swagger UI | `http://localhost:8080/swagger-ui.html` | Interactive API docs |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` | Raw API specification |
| Prometheus | `http://localhost:9090` | Metrics query engine |
| Grafana | `http://localhost:3000` | Dashboards for metrics, logs, analytics, quality |
| Loki | `http://localhost:3100` | Log storage (readiness check) |
| Alloy | `http://localhost:12345` | Docker log collector |
| Uptime Kuma | `http://localhost:3002` | Availability monitoring |
| Matomo | `http://localhost:8082` | Web analytics dashboard |
| Matomo exporter | `http://localhost:9101/metrics` | Analytics Prometheus metrics |
| Quality runner | `http://localhost:9102/metrics` | Frontend quality Prometheus metrics |
| SonarQube | `http://localhost:9000` | Code quality dashboard |
| Homepage | `http://localhost:3001` | Local tools launchpad |

### Run backend tests

```bash
cd backend
./mvnw clean test
```

### Build frontend

```bash
cd frontend
npm install
npm run build
```

### Run frontend quality checks once

```bash
docker compose run --rm quality-runner npm run run-once
```

## API endpoints

### Auth (`/auth`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/auth/signup` | Public | Register a new user |
| `POST` | `/auth/login` | Public | Login, sets JWT HttpOnly cookie |
| `POST` | `/auth/logout` | Authenticated | Logout, clears session and cookie |
| `GET` | `/auth/session` | Authenticated | Get current session user |

### Project endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/project-types` | Public | List all project types |
| `POST` | `/project-types` | Admin | Create a project type (multipart) |
| `DELETE` | `/project-types/{slug}` | Admin | Delete a project type by slug |
| `GET` | `/project-types/{slug}/projects` | Public | List projects by type slug |
| `GET` | `/projects/{slug}` | Public | Get a project by slug |
| `POST` | `/projects` | Admin | Create a project (multipart) |
| `DELETE` | `/projects/{slug}` | Admin | Delete a project by slug |

### Deprecated compatibility endpoints (kept for backward compatibility)

| Method | Path | Auth |
|--------|------|------|
| `GET` | `/project/projectTypes` | Public |
| `POST` | `/project/addProjectType` | Admin |
| `POST` | `/project/deleteProjectType` | Admin |
| `GET` | `/project/projects/{slug}` | Public |
| `GET` | `/project/getProject/{slug}` | Public |
| `POST` | `/project/addProject` | Admin |
| `POST` | `/project/deleteProject` | Admin |

### Storage

| Method | Path | Auth |
|--------|------|------|
| `GET` | `/storage/**` | Public |

## Security

- **JWT** stored in HttpOnly cookie (not accessible from JavaScript).
- **DB sessions** tracked in the `session` table.
- **One active session per user** — logging in replaces any existing session.
- **Admin endpoints** protected with `@PreAuthorize("hasRole('ADMIN')")`.
- **Passwords** hashed with BCrypt + HMAC-SHA256 pepper.
- **CORS** configured in the backend (`app.cors.origin`).
- **CSRF** disabled (acceptable because the JWT is stored in an HttpOnly cookie).

## Secrets warning

Secrets (passwords, tokens, JWT keys) are stored in `.env.passwords` (gitignored).
Non-secret config is stored in `.env` (gitignored).
Templates are provided as `.env.example` and `.env.passwords.example`.

**Never commit real secrets.** Do not expose `/actuator/prometheus`, Prometheus (port 9090), or Grafana (port 3000) publicly without authentication or a reverse proxy.

## Documentation

- [Tooling Architecture](documentation/tooling.md): full selected stack
- [Docker setup](documentation/docker.md): compose files, commands, monitoring workflow
- [Local Quality Tooling](documentation/local_quality.md): SonarQube, JaCoCo, quality-runner
- [Security Scanning](documentation/security.md): Trivy workflow
- [Database schema](documentation/db.md)
- [Flyway migrations](documentation/flyway.md)
- [Project structure](documentation/project_structure.md)
- [Git workflow](documentation/git_structure.md)
- [Roadmap](documentation/todo.md)

## Access model

| Visibility | Access |
|------------|--------|
| Public | Anyone (no auth required) |
| Private | Authenticated users only |
| Admin | Admin users (CRUD on projects/types) |
