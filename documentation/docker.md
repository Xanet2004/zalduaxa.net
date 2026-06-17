# Docker (development and deploy)

[Back to menu](/README.md)

## Overview

The project uses six Docker Compose files, all under the project name `zalduaxa-net`:

| File | Purpose |
|------|---------|
| `docker-compose.yml` | **Main entrypoint.** Includes all other files. Core: postgres, backend, frontend |
| `docker-compose.dev.yml` | PostgreSQL-only helper for local backend development |
| `docker-compose.quality.yml` | SonarQube + DB, quality-runner |
| `docker-compose.tools.yml` | Homepage local tools dashboard |
| `docker-compose.monitoring.yml` | Prometheus, Grafana, Loki, Alloy, Uptime Kuma |
| `docker-compose.analytics.yml` | Matomo + DB + cron + Prometheus exporter |

## Ports

| Service | Port |
|---------|------|
| Frontend | `http://localhost:5173` |
| Backend | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| PostgreSQL | `localhost:5432` |
| SonarQube | `http://localhost:9000` |
| Prometheus | `http://localhost:9090` |
| Grafana | `http://localhost:3000` |
| Loki | `http://localhost:3100` |
| Alloy | `http://localhost:12345` |
| Uptime Kuma | `http://localhost:3002` |
| Matomo | `http://localhost:8082` |
| Matomo exporter | `http://localhost:9101/metrics` |
| Frontend quality runner | `http://localhost:9102/metrics` |
| Homepage | `http://localhost:3001` |

---

## Environment file split

Secrets and non-secret config are split across separate files:

| File | Contents | Tracked? |
|------|----------|----------|
| `.env` | Non-secret runtime config (CORS, cookie, DB URL, log level) | No (gitignored; template is `.env.example`) |
| `.env.passwords` | Local secrets/passwords (JWT secret, pepper, DB password) | No (gitignored; template is `.env.passwords.example`) |
| `.env.example` | Tracked template for non-secret vars | Yes |
| `.env.passwords.example` | Tracked template for secrets | Yes |
| `.env.production` | Production non-secret config | No |
| `.env.production.passwords` | Production secrets | No |

### Key environment variables

| Variable | Purpose |
|----------|---------|
| `SPRING_PROFILES_ACTIVE` | Selects `dev` or `prod` profile (default: `dev`) |
| `APP_ENV_FILE` | Path to the non-secret env file (default: `.env`) |
| `APP_PASSWORD_ENV_FILE` | Path to the secrets env file (default: `.env.passwords`) |
| `APP_JWT_SECRET` | Key for signing JWT tokens |
| `APP_PASSWORD_PEPPER` | HMAC-SHA256 key applied before BCrypt |
| `APP_CORS_ORIGIN` | Allowed CORS origin (default: `http://localhost:5173`) |

---

## Workflow 1: Full local stack

Start the complete local environment:

```bash
docker compose up -d --build
```

This builds and starts all services:

- **postgres**: App PostgreSQL 16 with persistent volume
- **backend**: Spring Boot on port 8080
- **frontend**: Built with Vite, served by nginx on port 5173
- **sonarqube-db**: SonarQube PostgreSQL 16 database
- **sonarqube**: SonarQube community on port 9000
- **homepage**: Tools dashboard on port 3001
- **prometheus**: Metrics storage and query engine on port 9090
- **grafana**: Runtime metrics dashboards on port 3000
- **loki**: Log storage on port 3100
- **alloy**: Docker log collector on port 12345
- **uptime-kuma**: Availability monitoring on port 3002
- **matomo-db**: Matomo MariaDB database
- **matomo**: Web analytics on port 8082
- **matomo-cron**: Hourly analytics archive
- **matomo-exporter**: Analytics Prometheus metrics on port 9101
- **quality-runner**: Frontend quality checks on port 9102

Stop all services:

```bash
docker compose stop
```

To stop and remove containers (preserves volumes):

```bash
docker compose down
```

**Warning**: `docker compose down -v` deletes volumes for ALL included services, including the app database, SonarQube data, Prometheus data, Grafana data, Loki data, and Matomo data.

---

## Workflow 2: Local backend + database in Docker

Run only PostgreSQL in Docker while developing the backend locally:

```bash
docker compose -f docker-compose.dev.yml up -d
```

Then start the backend:

```bash
cd backend
./mvnw spring-boot:run
```

The dev compose file uses:
- Database name: `zalduaxa_net_dev`
- User: `app` / password: `app`

Stop the dev database:

```bash
docker compose -f docker-compose.dev.yml down
```

To delete the dev database volume (resets all data):

```bash
docker compose -f docker-compose.dev.yml down -v
```

---

## Frontend nginx proxy

When running in Docker, the frontend nginx proxies API requests:

```
Browser → /api/project-types → nginx → http://backend:8080/project-types
```

- The frontend always calls `/api/...` (e.g. `/api/project-types`, `/api/auth/login`).
- Nginx strips the `/api` prefix and forwards to the backend.
- The backend receives paths without the `/api` prefix.

**Note for local frontend development:** The Vite dev server currently does not define a proxy. When running the frontend locally (`npm run dev`), `/api/...` requests go to `localhost:5173` instead of the backend on `localhost:8080`. To test the full stack locally, run it in Docker or add a Vite proxy configuration.

---

## Storage volume

The local `./storage` directory is mounted into the backend container at `/app/storage`:

```yaml
volumes:
  - ./storage:/app/storage
```

Files placed in `./storage/` on the host are served by the backend at `/storage/**`.

---

## Docker Compose organization

`docker-compose.yml` is the main entrypoint. It uses the `include` directive to compose in `docker-compose.quality.yml`, `docker-compose.tools.yml`, `docker-compose.monitoring.yml`, and `docker-compose.analytics.yml`. Running `docker compose up -d --build` from the repository root starts all services under the single project name `zalduaxa-net`.

Services are organized across shared and dedicated networks:

| Network | Services |
|---------|----------|
| `zalduaxa-net_default` | Core app: backend, frontend, postgres, prometheus, grafana, loki, alloy, uptime-kuma, matomo, matomo-exporter, quality-runner |
| `zalduaxa-net-quality` | Quality tooling: SonarQube, SonarQube DB |
| `zalduaxa-net-tools` | Tools dashboard: Homepage |

Most monitoring and analytics services share the default network with the application, so they can reach each other via Docker DNS. Separate networks keep quality and tooling containers isolated.

### Individual file usage

Any compose file can be used standalone if needed:

```bash
docker compose -f docker-compose.quality.yml up -d
docker compose -f docker-compose.tools.yml up -d
docker compose -f docker-compose.monitoring.yml up -d
docker compose -f docker-compose.analytics.yml up -d
```

See [Local Quality Tooling](local_quality.md) for the full quality workflow.

### Safe volume guidance

**Warning**: Running `docker compose down -v` from the main file will delete volumes for ALL included services, including the app database, SonarQube data, Prometheus data, Grafana data, Loki data, Alloy data, Uptime Kuma data, and Matomo data. To target a specific file only:

```bash
docker compose -f docker-compose.quality.yml down -v     # resets SonarQube only
docker compose -f docker-compose.dev.yml down -v         # resets dev database only
```

---

## Monitoring stack

Prometheus, Grafana, Loki, Alloy, and Uptime Kuma run as part of `docker-compose.monitoring.yml`, included automatically by the main compose file.

### Architecture

```
backend (port 8080)
  └─ /actuator/prometheus  ──scrape──►  prometheus (port 9090)
                                            │
                                      http://prometheus:9090
                                            │
                                       grafana (port 3000)
                                            │
                                  Prometheus datasource (provisioned)
                                            │
                                  ┌─────────┴──────────┐
                                  │                    │
                    Grafana dashboards:          Other data sources:
                    Backend Metrics              Loki (logs)
                    Frontend Quality             Prometheus itself
                    Matomo Analytics
                    Logs Overview
```

### Prometheus

Prometheus scrapes:
- Backend Actuator metrics at `http://backend:8080/actuator/prometheus`
- Loki metrics at `http://loki:3100/metrics`
- Alloy metrics at `http://alloy:12345/metrics`
- Uptime Kuma metrics at `http://uptime-kuma:3001/metrics` (authenticated)
- Matomo exporter at `http://matomo-exporter:9101/metrics`
- Quality runner at `http://quality-runner:9102/metrics`

### Grafana dashboards

| Dashboard | Data source | Description |
|-----------|-------------|-------------|
| Backend Metrics | Prometheus | JVM memory, HTTP throughput/latency, errors, uptime, DB pool, Tomcat sessions |
| Frontend Quality | Prometheus | Axe violations, Lighthouse scores, route availability |
| Logs Overview | Loki | Centralized log browser and search |
| Matomo Analytics | Prometheus | Visit stats, unique visitors, bounce rate, top pages |
| Matomo Overview | Prometheus | Aggregate Matomo metrics view |

All dashboards are auto-provisioned and appear in Grafana under the `Zalduaxa.net` folder.

### Loki + Alloy (centralized logging)

Alloy reads Docker container logs via the Docker socket and forwards them to Loki. Loki stores the logs and makes them queryable in Grafana.

```bash
# Open Grafana → Explore → select Loki data source
# Query: {container_name=~".+"}
```

Available log labels from Alloy include `container_name`, `container_image`, `compose_service`, and standard log level parsing.

### Uptime Kuma (availability monitoring)

Uptime Kuma monitors endpoint availability. Configured monitors include:
- Frontend (`http://frontend:5173`)
- Backend health (`http://backend:8080/actuator/health`)
- Prometheus readiness (`http://prometheus:9090/-/ready`)
- Grafana health (`http://grafana:3000/api/health`)

Metrics are exposed at `/metrics` and scraped by Prometheus.

### Health checks

```bash
# Backend Actuator health
curl -i http://localhost:8080/actuator/health

# Prometheus readiness
curl -i http://localhost:9090/-/ready

# Grafana API health
curl -i http://localhost:3000/api/health

# Loki readiness
curl -i http://localhost:3100/ready
```

### Prometheus validation

Open `http://localhost:9090` in a browser and run the query:

```
up
```

Expected result: all targets show value `1`.

### Grafana validation

1. Open `http://localhost:3000`.
2. Log in with the Grafana admin credentials from `.env.passwords`.
3. Browse dashboards under `Zalduaxa.net` folder.
4. Run a query in **Explore** using the Prometheus data source.

---

## Analytics stack (Matomo)

Matomo runs as part of `docker-compose.analytics.yml`. It consists of:

| Service | Purpose |
|---------|---------|
| `matomo-db` | MariaDB database for Matomo data |
| `matomo` | Matomo web analytics application (port 8082) |
| `matomo-cron` | Hourly archive processing |
| `matomo-exporter` | Python HTTP server exposing Matomo metrics for Prometheus |

### Frontend tracking

Matomo tracking is enabled via build-time environment variables passed to the frontend Dockerfile:

```bash
VITE_MATOMO_ENABLED=true
VITE_MATOMO_URL=http://localhost:8082/
VITE_MATOMO_SITE_ID=1
```

These default to disabled in `.env`. Set `VITE_MATOMO_ENABLED=true` to enable tracking.

### Matomo exporter metrics

The exporter queries the Matomo API every 60 seconds and exposes Prometheus metrics on port 9101:
- `matomo_visits`, `matomo_unique_visitors`, `matomo_actions`
- `matomo_bounces`, `matomo_avg_visit_duration_seconds`
- `matomo_page_hits`, `matomo_page_visits`

### Grafana dashboards

Provisioned dashboards: "Matomo Analytics" and "Matomo Overview" in the `Zalduaxa.net` folder.

---

## Frontend quality runner

The quality-runner (`docker-compose.quality.yml`) is a Node.js service that periodically checks the frontend:

| Check | What it does |
|-------|-------------|
| Route health | Opens each route and checks for JS/page/network errors |
| Accessibility | Scans each route with `@axe-core/playwright` |
| Lighthouse | Runs performance, accessibility, best-practices, and SEO audits |

Metrics are exposed on port 9102 and scraped by Prometheus every 60 seconds. The Grafana "Frontend Quality" dashboard visualizes the results.

### Starting and stopping

```bash
docker compose up -d quality-runner
docker compose logs --tail=100 quality-runner
```

### Running a one-off check

```bash
docker compose run --rm quality-runner npm run run-once
```

### Configuration

See `.env.example` for all quality-runner variables (`QUALITY_*`).

---

## Profiles

The backend has two Spring profiles:

| Profile | Defaults | Purpose |
|---------|----------|---------|
| `dev` | Active by default | CORS allows `localhost:5173`, DB runs on port 5432, seed users created, DEBUG logging, Swagger enabled |
| `prod` | Switch via `SPRING_PROFILES_ACTIVE=prod` | CORS points to production origin, seeds disabled, WARN logging, Swagger disabled |

The default profile is set in `application.properties`:

```properties
spring.profiles.default=dev
```

Override at runtime:

```bash
SPRING_PROFILES_ACTIVE=prod docker compose up --build
```

## Flyway database reset

Flyway manages the database schema. To reset the development database:

```bash
docker compose down -v
docker compose up --build
```

- `down -v` deletes the PostgreSQL Docker volume, including all data and the Flyway history table.
- On next startup, Flyway recreates the schema from scratch.

**Never run `down -v` in production** — it destroys all data.

---

## Useful Docker commands

```bash
# List running containers
docker ps

# View service logs
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres
docker compose logs -f quality-runner

# Rebuild and restart a single service
docker compose up -d --build backend
docker compose up -d --build frontend

# Open a shell in a container
docker exec -it zalduaxa-net-backend sh
docker exec -it zalduaxa-net-frontend sh
docker exec -it zalduaxa-net-postgres bash
```
