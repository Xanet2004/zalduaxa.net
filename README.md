# zalduaxa.net

A personal portfolio/project platform. Built with Spring Boot, React, PostgreSQL, and Docker.

## Current stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3, Java 21, PostgreSQL, Flyway, Spring Security JWT, BCrypt + pepper |
| Frontend | React, TypeScript, Vite, nginx |
| Infrastructure | Docker Compose, env-file split, local storage volume |

## Quick start

```bash
cp .env.example .env
cp .env.passwords.example .env.passwords
docker compose up --build
```

Access the app at `http://localhost:5173`.

### Run backend tests

```bash
cd backend
mvn clean test
```

Expected: **106 tests, 0 failures, 0 errors**.

### Build frontend

```bash
cd frontend
npm install
npm run build
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

## Documentation

- [Project structure](/documentation/project_structure.md)
- [Docker setup](/documentation/docker.md)
- [Database schema](/documentation/db.md)
- [Flyway migrations](/documentation/flyway.md)
- [Tooling Architecture](/documentation/tooling.md): selected stack for CI/CD, quality, observability, monitoring, analytics, testing, and API documentation
- [Git workflow](/documentation/git_structure.md)
- [Roadmap](/documentation/todo.md)

## Access model

| Visibility | Access |
|------------|--------|
| Public | Anyone (no auth required) |
| Private | Authenticated users only |
| Admin | Admin users (CRUD on projects/types) |
