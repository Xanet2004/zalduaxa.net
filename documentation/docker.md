# Docker (development and deploy)
[Back to menu](/README.md)

# Docker

This document explains how to run **PostgreSQL in Docker for development** and how to **build + run the full stack (frontend + backend + PostgreSQL) in Docker** for deployment.

---

## 1) Development setup (local backend + local frontend + PostgreSQL in Docker)

In development, you run:
- **Backend** (Spring Boot) locally
- **Frontend** (Vite) locally
- **PostgreSQL** in Docker (so your DB is always consistent)

### 1.1 Start PostgreSQL (dev)

From the repository root:

```bash
docker compose -f docker-compose.dev.yml up -d
```

Check it is running:

```bash
docker ps
```

(Optional) Check logs:

```bash
docker logs -f zalduaxa-net-postgres-dev
```

### 1.2 Backend database configuration (dev)

If your backend runs locally, it should connect to PostgreSQL via `localhost`:

**Example (application-dev.properties):**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/zalduaxa_net_dev
spring.datasource.username=app
spring.datasource.password=app
```

Run backend with the dev profile (example):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 1.3 Frontend (dev)

Run frontend normally (example):

```bash
cd frontend
npm install
npm run dev
```

### 1.4 Stop development DB

```bash
docker compose -f docker-compose.dev.yml down
```

To also delete the dev database volume (WARNING: deletes dev data):

```bash
docker compose -f docker-compose.dev.yml down -v
```

---

## 2) Deploy setup (frontend + backend + PostgreSQL in Docker)

In production (or a full local Docker run), the three services run inside containers:
- **postgres**: database with persistent volume
- **backend**: Spring Boot container
- **frontend**: built with Vite and served by Nginx

### 2.1 Create environment file

Create a file named `.env` in the repository root:

```env
POSTGRES_PASSWORD=put_a_strong_password_here
```

> Do not commit `.env` to Git. Add it to `.gitignore`.

### 2.2 Build and run all containers

From the repository root:

```bash
docker compose up -d --build
```

Check status:

```bash
docker compose ps
```

Check logs:

```bash
docker compose logs -f
```

### 2.3 Access the services

- Frontend: `http://localhost/`
- Backend (if exposed): `http://localhost:8080/`

> If your frontend reverse-proxies `/api` to the backend, you will mostly use the frontend URL.

### 2.4 Stop production stack

```bash
docker compose down
```

To also delete the production database volume (WARNING: deletes prod data):

```bash
docker compose down -v
```

---

## 3) Rebuild a single service (optional)

Rebuild + restart backend:

```bash
docker compose up -d --build backend
```

Rebuild + restart frontend:

```bash
docker compose up -d --build frontend
```

---

## 4) Useful Docker commands

List running containers:

```bash
docker ps
```

See service logs:

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres
```

Open a shell inside a container:

```bash
docker exec -it zalduaxa-net-backend sh
docker exec -it zalduaxa-net-frontend sh
docker exec -it zalduaxa-net-postgres bash
```
