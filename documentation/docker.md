# Docker (development and deploy)

[Back to menu](/README.md)

## Overview

The project uses two Docker Compose files:

| File | Purpose |
|------|---------|
| `docker-compose.yml` | Full stack: PostgreSQL + backend + frontend (for deploy or full local run) |
| `docker-compose.dev.yml` | PostgreSQL-only helper for local backend development |

## Ports

| Service | Port |
|---------|------|
| Frontend | `http://localhost:5173` |
| Backend | `http://localhost:8080` |
| PostgreSQL | `localhost:5432` |

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

## Workflow 1: Full Docker stack

Run all three services (PostgreSQL + backend + frontend) in containers:

```bash
docker compose up --build
```

This builds and starts:
- **postgres**: PostgreSQL 16 with persistent volume
- **backend**: Spring Boot on port 8080
- **frontend**: Built with Vite, served by nginx on port 5173

Access the app at `http://localhost:5173`.

Stop:

```bash
docker compose down
```

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
