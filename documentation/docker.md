# Docker (development and deploy)

[Back to menu](/README.md)

## Overview

The project uses five Docker Compose files, all under the project name `zalduaxa-net`:

| File | Purpose |
|------|---------|
| `docker-compose.yml` | **Main entrypoint.** Includes quality/tools/monitoring files. Starts: postgres, backend, frontend, SonarQube, SonarQube DB, Homepage, Prometheus, Grafana |
| `docker-compose.dev.yml` | PostgreSQL-only helper for local backend development |
| `docker-compose.quality.yml` | Local quality tooling: SonarQube + SonarQube DB (included by main file) |
| `docker-compose.tools.yml` | Local tools dashboard: Homepage (included by main file) |
| `docker-compose.monitoring.yml` | Monitoring stack: Prometheus, Grafana (included by main file) |

## Ports

| Service | Port |
|---------|------|
| Frontend | `http://localhost:5173` |
| Backend | `http://localhost:8080` |
| PostgreSQL | `localhost:5432` |
| SonarQube | `http://localhost:9000` |
| Prometheus | `http://localhost:9090` |
| Grafana | `http://localhost:3000` |
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
| `APP_AUTH_COOKIE_SECURE` | Whether auth cookie has the Secure flag (dev: `false`, prod: `true`) |

---

## Workflow 1: Full local stack

Start the complete local environment — app, quality tooling, and tools dashboard — in one command:

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

Access points:
- App frontend: `http://localhost:5173`
- Backend health: `http://localhost:8080/actuator/health`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- SonarQube: `http://localhost:9000`
- Homepage: `http://localhost:3001`

Stop all services:

```bash
docker compose stop
```

To stop and remove containers (preserves volumes):

```bash
docker compose down
```

**Warning**: `docker compose down -v` deletes volumes for ALL included services, including the app database and SonarQube data.

---

## Workflow 2: Local backend + database in Docker

Run only PostgreSQL in Docker while developing the backend locally:

```bash
docker compose -f docker-compose.dev.yml up -d
```

Then start the backend:

```bash
cd backend
mvn spring-boot:run
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

`docker-compose.yml` is the main entrypoint. It uses the `include` directive to compose in `docker-compose.quality.yml`, `docker-compose.tools.yml`, and `docker-compose.monitoring.yml`. Running `docker compose up -d --build` from the repository root starts all services under the single project name `zalduaxa-net`.

Services are organized across shared and dedicated networks:

| Network | Services |
|---------|----------|
| `zalduaxa-net_default` | Core app: backend, frontend, postgres, prometheus, grafana |
| `zalduaxa-net-quality` | Quality tooling: SonarQube, SonarQube DB |
| `zalduaxa-net-tools` | Tools dashboard: Homepage |

Prometheus and Grafana share the default network with the application, so they can reach the backend at `backend:8080` and Prometheus at `prometheus:9090` via Docker DNS. Separate networks keep quality and tooling containers isolated.

### Individual file usage

The quality, tools, and monitoring compose files can be used standalone if needed:

```bash
docker compose -f docker-compose.quality.yml up -d
docker compose -f docker-compose.tools.yml up -d
docker compose -f docker-compose.monitoring.yml up -d
```

See [Local Quality Tooling](local_quality.md) for the full quality workflow.

### Safe volume guidance

**Warning**: Running `docker compose down -v` from the main file will delete volumes for ALL included services, including the app database and SonarQube data. To target a specific file only:

```bash
docker compose -f docker-compose.quality.yml down -v     # resets SonarQube only
docker compose -f docker-compose.dev.yml down -v         # resets dev database only
```

---

## Monitoring stack

Prometheus and Grafana run as part of `docker-compose.monitoring.yml`, included automatically by the main compose file.

### Starting and stopping

```bash
docker compose up -d prometheus grafana
docker compose logs --tail=100 prometheus
docker compose logs --tail=100 grafana
docker compose restart grafana
```

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
                                  Zalduaxa.net Backend Metrics dashboard
```

### Health checks

```bash
# Backend Actuator health
curl -i http://localhost:8080/actuator/health

# Prometheus readiness
curl -i http://localhost:9090/-/ready

# Grafana API health
curl -i http://localhost:3000/api/health
```

### Prometheus validation

Open `http://localhost:9090` in a browser and run the query:

```
up
```

Expected result: `zalduaxa-backend` target with value `1`.

### Grafana validation

1. Open `http://localhost:3000`.
2. Log in with the Grafana admin credentials from `.env.passwords`.
3. Go to **Connections → Data sources** and confirm `Prometheus` is present with URL `http://prometheus:9090`.
4. Open **Dashboards → Zalduaxa.net Backend Metrics**.
5. Run a query in **Explore**: `up` — should return the backend target.

### Volume safety warning

`docker compose down -v` deletes volumes for ALL included services, including PostgreSQL, SonarQube, Prometheus (`prometheus_data`), and Grafana (`grafana_data`). Use `-v` only when you intentionally want to reset everything.

---

## Profiles

The backend has two Spring profiles:

| Profile | Defaults | Purpose |
|---------|----------|---------|
| `dev` | Active by default | CORS allows `localhost:5173`, DB runs on port 5432, seed users created, DEBUG logging |
| `prod` | Switch via `SPRING_PROFILES_ACTIVE=prod` | CORS points to production origin, seeds disabled, WARN logging |

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

# Rebuild and restart a single service
docker compose up -d --build backend
docker compose up -d --build frontend

# Open a shell in a container
docker exec -it zalduaxa-net-backend sh
docker exec -it zalduaxa-net-frontend sh
docker exec -it zalduaxa-net-postgres bash
```
