# Tooling Architecture

Selected tools for quality, automation, observability, monitoring, analytics, testing, and API documentation.

[Back to menu](/README.md)

---

## 1. Purpose

This document defines the professional tooling architecture for Zalduaxa.net. The goal is to make the project behave like a serious, maintainable product with:

- Automatic validation on every change.
- Visibility into code quality and technical debt.
- Controlled dependency updates with security awareness.
- Vulnerability scanning for the repository and container images.
- Backend health metrics exposed at runtime.
- Dashboards for application and system metrics.
- Centralized log collection and querying.
- External uptime monitoring for public endpoints.
- Privacy-friendly web analytics.
- Frontend quality checks: end-to-end tests, accessibility, and performance.
- Auto-generated API documentation from the backend code.

This document is the reference for what tools will be used, why they were chosen, and in what order they will be introduced.

---

## 2. Guiding principles

- Prefer self-hosted tools where reasonable.
- Avoid unnecessary enterprise complexity at the current scale.
- Choose tools that can scale later without rewriting the project.
- Keep the base application independent from optional monitoring components.
- Use Docker Compose overlays for optional tooling so the core stack stays lightweight.
- Make every tool useful, documented, and testable.
- Avoid installing tools only because they look impressive.

---

## 3. Selected stack overview

| Area | Selected tools | Purpose |
|------|---------------|---------|
| CI/CD | GitHub Actions | Automate validation on pull requests and pushes |
| Dependency automation | Dependabot | Keep Maven, npm, and Actions dependencies current |
| Code quality | SonarQube, JaCoCo, ESLint / TypeScript checks | Central quality dashboard, coverage, and linting |
| Security scanning | Trivy | Container image and filesystem vulnerability scanning |
| Backend health and metrics | Spring Boot Actuator, Micrometer Prometheus registry | Expose JVM, HTTP, and application metrics |
| Metrics storage and dashboards | Prometheus, Grafana | Time-series storage and visual dashboards |
| Centralized logging | Grafana Loki, Grafana Alloy | Log aggregation from all services |
| Uptime monitoring | Uptime Kuma | External availability checks |
| Web analytics | Matomo | Privacy-friendly, self-hosted visitor analytics |
| Frontend quality assurance | Playwright, axe-core, Lighthouse CI | End-to-end tests, accessibility, and performance audits |
| API documentation | springdoc-openapi | Generated OpenAPI docs from Spring controllers |

---

## 4. CI/CD and automation

**Selected tool: GitHub Actions.**

GitHub Actions will validate the backend and frontend automatically. It runs on every push and pull request to keep feedback fast.

The initial pipeline will include:

- Backend compilation and test execution.
- Frontend build verification.

Current workflows:

| Workflow | Triggers | Purpose |
|----------|----------|---------|
| `ci.yml` | Push, PR | Backend tests (`./mvnw clean test`), frontend build (`npm run build`) |
| `security.yml` | Push, PR, weekly, manual | Trivy filesystem scan (vulnerabilities, secrets, misconfigurations) |

---

## 5. Dependency management

**Selected tool: Dependabot.**

Dependabot will track updates for:

- Maven dependencies (`pom.xml`).
- npm dependencies (`package.json`).
- GitHub Actions versions.
- Docker base images where applicable.

Each update arrives as a pull request, making upgrades visible and reviewable. This keeps the dependency surface known and reduces the risk of drifting too far behind upstream releases.

---

## 6. Code quality

**Selected tools: SonarQube, JaCoCo, ESLint / TypeScript checks.**

SonarQube is the central quality dashboard. It provides visibility into code smells, bugs, vulnerabilities, duplicated code, and technical debt. JaCoCo supplies Java test coverage data so SonarQube can track coverage trends.

On the frontend side, TypeScript strict checks and ESLint (where configured) will catch type errors and common problems at build time.

### Current implementation status

| Component | Status | Details |
|-----------|--------|---------|
| Local SonarQube | Implemented | `docker-compose.quality.yml`, port 9000 |
| Backend JaCoCo | Implemented | `jacoco-maven-plugin` in `backend/pom.xml`, `verify` phase |
| `sonar-project.properties` | Implemented | Root-level, one project for monorepo |
| Homepage dashboard | Implemented | `docker-compose.tools.yml`, port 3001 |
| Frontend quality runner | Implemented | Playwright + axe + Lighthouse, port 9102 |
| Quality Gate enforcement | Postponed | Will be added together with CI integration |
| CI SonarQube integration | Postponed | Local-only; not reachable from GitHub-hosted runners |

See [Local Quality Tooling](local_quality.md) for the local workflow.

### Later stages

Quality gates will be introduced gradually. The goal is to prevent new critical issues from accumulating, not to block development because of legacy debt inherited from earlier phases.

---

## 7. Security scanning

**Selected tool: Trivy.**

Trivy scans the repository filesystem and container images for known vulnerabilities. It detects:

- Vulnerable operating-system packages and language libraries.
- Infrastructure misconfigurations.
- Exposed secrets (basic detection).

Trivy complements Dependabot by catching issues in the runtime container layer that Dependabot cannot see. It runs quickly and produces actionable output without requiring an external service.

### Current implementation status

| Component | Status | Details |
|-----------|--------|---------|
| Trivy repository scan | Implemented | `.github/workflows/security.yml` |
| Vulnerability scanning | Implemented | Maven/npm manifests and lockfiles |
| Secret scanning | Implemented | Repository filesystem scan |
| Misconfiguration scanning | Implemented | Dockerfiles, Compose files and GitHub Actions workflows |
| SARIF upload | Implemented | Uploaded to GitHub Code Scanning when available |
| Docker image scanning | Postponed | Will be added after repository scanning is stable |

The first implementation intentionally fails only on CRITICAL findings. HIGH findings are reported but do not block the workflow yet.

See [Security Scanning](security.md) for the full workflow.

---

## 8. Backend health and metrics

**Selected tools: Spring Boot Actuator, Micrometer Prometheus registry.**

These are the standard Spring Boot mechanism for exposing runtime health and metrics. Key endpoints:

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Liveness and readiness — database, disk, general health |
| `/actuator/info` | Build and application metadata |
| `/actuator/prometheus` | Prometheus-formatted metrics |

Security considerations:

- The health endpoint can be public or semi-public for external monitoring.
- The Prometheus scraping endpoint must be internal-only or protected.
- Actuator endpoints are implemented and enabled. Do not expose `/actuator/prometheus`, Prometheus (port 9090), or Grafana (port 3000) publicly without reverse proxy protection or authentication. In local development they are exposed for convenience.

---

## 9. Metrics and dashboards

**Selected tools: Prometheus, Grafana.**

Prometheus scrapes metrics from the backend Actuator endpoint at `http://backend:8080/actuator/prometheus` and stores them as time-series data. Grafana connects to Prometheus as a data source and provides visual dashboards.

### Implementation status

| Component | Status | Details |
|-----------|--------|---------|
| Prometheus | Implemented | `docker-compose.monitoring.yml`, port 9090, scraping backend |
| Grafana | Implemented | `docker-compose.monitoring.yml`, port 3000 |
| Prometheus datasource | Provisioned | Auto-connected to `http://prometheus:9090` |
| Backend metrics dashboard | Provisioned | "Zalduaxa.net Backend Metrics" |

### Grafana dashboard

The provisioned dashboard covers:

- JVM heap and non-heap memory usage.
- HTTP request throughput and latency (p99).
- HTTP 5xx error rate.
- Backend uptime.
- Database connection pool health (HikariCP).
- Tomcat active sessions.

### Security

- Grafana admin credentials come from `.env.passwords` (`GRAFANA_ADMIN_USER`, `GRAFANA_ADMIN_PASSWORD`), not from committed compose files.
- Sign-up is disabled (`GF_USERS_ALLOW_SIGN_UP=false`).
- Anonymous access is disabled (`GF_AUTH_ANONYMOUS_ENABLED=false`).
- Do not expose Grafana or Prometheus publicly without authentication or a reverse proxy.

### Local URLs

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

---

## 10. Centralized logging

**Selected tools: Grafana Loki, Grafana Alloy.**

### Implementation status

| Component | Status | Details |
|-----------|--------|---------|
| Loki | Implemented | `docker-compose.monitoring.yml`, port 3100 |
| Alloy | Implemented | `docker-compose.monitoring.yml`, port 12345 |
| Logs dashboard | Implemented | Grafana "Logs Overview" |

### Architecture

Alloy reads Docker container logs via the Docker socket and forwards them to Loki. Loki stores and indexes the log data for querying in Grafana. Services collected:

- Backend Spring Boot stdout.
- Frontend nginx access and error logs.
- Prometheus and Grafana logs.
- All other Docker containers.

The Grafana logs dashboard allows searching by service, container, or log level. This replaces relying on `docker logs` for debugging.

---

## 11. Uptime monitoring

**Selected tool: Uptime Kuma.**

### Implementation status

| Component | Status | Details |
|-----------|--------|---------|
| Uptime Kuma | Implemented | `docker-compose.monitoring.yml`, port 3002 |

Uptime Kuma periodically checks endpoint availability and exposes metrics for Prometheus scraping. Configured monitors in the UI include the frontend, backend, and other critical services.

Services monitored:

- Frontend (`http://frontend:5173`)
- Backend health (`http://backend:8080/actuator/health`)
- Prometheus (`http://prometheus:9090/-/ready`)
- Grafana (`http://grafana:3000/api/health`)

Uptime Kuma metrics are scraped by Prometheus and displayed in Grafana. Alerts can be configured per monitor (email, Discord, Telegram, etc.).

---

## 12. Web analytics

**Selected tool: Matomo.**

### Implementation status

| Component | Status | Details |
|-----------|--------|---------|
| Matomo | Implemented | `docker-compose.analytics.yml`, port 8082 |
| Matomo DB | Implemented | MariaDB, persistent volume |
| Matomo cron | Implemented | Hourly archive processing |
| Matomo frontend tracking | Implemented | Vite build args for Matomo URL and site ID |
| Matomo exporter | Implemented | Python HTTP server, port 9101, scraped by Prometheus |
| Analytics dashboards | Implemented | Grafana "Matomo Analytics" and "Matomo Overview" |

### Architecture

Matomo runs as a Docker service with a MariaDB database. The `matomo-cron` container archives analytics data hourly. Frontend tracking is enabled via build-time environment variables (`VITE_MATOMO_ENABLED`, `VITE_MATOMO_URL`, `VITE_MATOMO_SITE_ID`).

The Matomo exporter (`monitoring/matomo-exporter/matomo_exporter.py`) queries the Matomo API and exposes Prometheus metrics:

- `matomo_visits`, `matomo_unique_visitors`, `matomo_actions`
- `matomo_bounces`, `matomo_avg_visit_duration_seconds`
- `matomo_page_hits`, `matomo_page_visits` (top pages)
- `matomo_exporter_up`, `matomo_exporter_scrape_duration_seconds`

### Security

- Matomo API token comes from `.env.passwords` (`MATOMO_TOKEN_AUTH`).
- Matomo is not exposed publicly in local development.
- A privacy/cookie consent mechanism may be needed before production deployment.

---

## 13. Frontend quality assurance

**Selected tools: Playwright, axe-core, Lighthouse.**

### Implementation status

| Component | Status | Details |
|-----------|--------|---------|
| Playwright smoke tests | Implemented | Routes: `/`, `/projects`, `/login`, `/signup`, `/__quality_not_found__` |
| axe accessibility scans | Implemented | Violations tracked per route by impact and rule |
| Lighthouse audits | Implemented | Scores for performance, accessibility, best-practices, SEO |
| Prometheus metrics | Implemented | Exported on port 9102 |
| Grafana dashboard | Implemented | "Frontend Quality" with axe violations, Lighthouse scores, route status |

### Architecture

The quality-runner (`monitoring/quality-runner/`) is a Node.js service based on the Playwright Docker image. It runs on a configurable interval (default 15 minutes) and checks:

1. **Route health** — each route loads without JS/page/network errors (`quality_route_up`).
2. **Accessibility** — `@axe-core/playwright` scans each route for violations (`quality_axe_violations_total`, `quality_axe_violations_by_impact`, `quality_axe_violations_by_rule`).
3. **Lighthouse** — performance, accessibility, best-practices, and SEO scores (`quality_lighthouse_score`). Timing metrics and cumulative layout shift are also reported.

Metrics are exposed via an HTTP server at `/metrics` and scraped by Prometheus. The Grafana "Frontend Quality" dashboard visualizes the results.

### Configuration

Variables in `.env`:

| Variable | Default | Description |
|----------|---------|-------------|
| `QUALITY_RUNNER_PORT` | `9102` | HTTP metrics port |
| `QUALITY_FRONTEND_BASE_URL` | `http://frontend:5173` | Frontend URL to test |
| `QUALITY_RUNNER_INTERVAL_SECONDS` | `900` | Run interval (15 min) |
| `QUALITY_ROUTE_TIMEOUT_MS` | `30000` | Per-route timeout |
| `QUALITY_ROUTES` | `/,/projects,/login,/signup,/__quality_not_found__` | Routes to check |
| `QUALITY_LIGHTHOUSE_ROUTES` | `/,/projects,/login,/signup` | Routes for Lighthouse |
| `QUALITY_LIGHTHOUSE_ENABLED` | `true` | Enable Lighthouse audits |
| `QUALITY_AXE_ENABLED` | `true` | Enable axe accessibility scans |

### Running a one-off check

```bash
docker compose run --rm quality-runner npm run run-once
```

### Security

- The quality-runner is for local development only. Do not expose port 9102 publicly.
- Lighthouse requires significant memory; the container uses `shm_size: "1gb"`.

---

## 14. API documentation

**Selected tool: springdoc-openapi.**

### Implementation status

| Component | Status | Details |
|-----------|--------|---------|
| OpenAPI generation | Implemented | Enabled in `dev` profile |
| Swagger UI | Implemented | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | Implemented | `http://localhost:8080/v3/api-docs` |

springdoc-openapi generates OpenAPI 3 documentation from Spring Boot controller annotations, request DTOs, and response types. This keeps the API documentation in sync with the code.

### Routes

| Path | Purpose |
|------|---------|
| `/v3/api-docs` | OpenAPI JSON schema |
| `/swagger-ui.html` | Interactive API explorer |

### Security

- Swagger UI and OpenAPI JSON are **enabled in the `dev` profile only** (`springdoc.api-docs.enabled=true`, `springdoc.swagger-ui.enabled=true`).
- In production (`prod` profile) they are **disabled** (`springdoc.api-docs.enabled=false`).

---

## 15. Runtime architecture

The following diagram shows the target tooling architecture. Components marked with ✔ are implemented. The others will be built incrementally.

```
GitHub
├── GitHub Actions ✔
├── Dependabot ✔
└── Repository checks ✔

Docker runtime
├── frontend nginx ✔
├── backend Spring Boot ✔
│   ├── Actuator ✔
│   ├── Prometheus metrics ✔
│   └── OpenAPI docs ✔
├── PostgreSQL ✔
├── Prometheus ✔
├── Grafana ✔
├── Loki ✔
├── Alloy ✔
├── Uptime Kuma ✔
├── Matomo ✔
│   └── Matomo exporter ✔
├── quality-runner ✔
└── SonarQube ✔
```

---

## 16. Docker Compose strategy

Each group of tools will live in its own Compose file so the base application stack stays clean:

| File | Contents |
|------|----------|
| `docker-compose.yml` | Base application: postgres, backend, frontend |
| `docker-compose.monitoring.yml` | Prometheus, Grafana, Loki, Alloy, Uptime Kuma |
| `docker-compose.quality.yml` | SonarQube + DB, quality-runner |
| `docker-compose.tools.yml` | Homepage local tools dashboard |
| `docker-compose.analytics.yml` | Matomo + DB + cron + Prometheus exporter |

All five compose files are implemented. `docker-compose.yml` includes the other four via the `include` directive.

Tooling compose files extend the base project where needed and can be started independently:

```bash
docker compose -f docker-compose.monitoring.yml up -d
```

This keeps the base `docker compose up --build` fast and focused on the application itself.

---

## 17. Implementation order

All tools have been introduced in this sequence. Each step builds on the previous one:

1. **GitHub Actions CI** — Backend tests and frontend build on every push. ✔ Implemented.
2. **Dependabot** — Automated dependency update pull requests. ✔ Implemented.
3. **SonarQube + JaCoCo** — Quality dashboard with coverage reporting. ✔ Implemented locally; CI integration postponed.
4. **Trivy** — Vulnerability scanning in CI. ✔ Implemented.
5. **Spring Boot Actuator + Micrometer** — Health and metrics endpoints. ✔ Implemented.
6. **Prometheus + Grafana** — Metrics storage and dashboards. ✔ Implemented.
7. **Loki + Alloy** — Centralized log collection. ✔ Implemented.
8. **Uptime Kuma** — External availability monitoring. ✔ Implemented.
9. **Matomo** — Self-hosted web analytics with Prometheus exporter. ✔ Implemented.
10. **Playwright + axe-core + Lighthouse** — Frontend quality runner with Prometheus metrics. ✔ Implemented.
11. **springdoc-openapi** — Generated API documentation (dev profile only). ✔ Implemented.
12. **Final documentation** — This document covers the full stack.

---

## 18. Future extensions

These tools are not selected for the current implementation but are kept in mind for later stages:

| Tool | When it makes sense |
|------|---------------------|
| **Sentry** | When real production error tracking is needed — captures frontend and backend errors with context, supports performance monitoring. |
| **OpenTelemetry + Grafana Tempo** | When the application grows to multiple services and distributed tracing is needed to debug latency across service boundaries. |
| **PostHog** | When product analytics, funnels, session replays, or feature flags become necessary — more powerful than Matomo for product decisions. |
| **Zabbix** | When broader infrastructure and network monitoring is needed for the host system and NAS. |
| **VictoriaMetrics / Grafana Mimir** | When Prometheus retention or scale becomes a limitation — these provide long-term, highly available metrics storage. |
| **Dependency-Track** | When SBOM management and enterprise dependency governance are required. |

---

## 19. Tools not selected for now

These tools were evaluated and intentionally set aside for the current stage:

| Tool | Reason |
|------|--------|
| **Jenkins** | GitHub Actions provides the same capability with simpler configuration for this repository. Jenkins would add unnecessary server maintenance. |
| **ELK / OpenSearch** | Loki is lighter for the current log volume, integrates natively with Grafana, and avoids running a full Elasticsearch cluster. |
| **Kubernetes** | Docker Compose is sufficient for a single-node deployment. Kubernetes would add orchestration complexity without benefit at this scale. |
| **Datadog / New Relic** | Powerful platforms but external SaaS with per-host pricing. They conflict with the self-hosting preference and are not justified for a personal portfolio. |
| **Google Analytics** | Not aligned with the privacy and self-hosting goals of the project. Matomo provides equivalent data without third-party data sharing. |
| **Renovate** | More configurable than Dependabot, but Dependabot is simpler and sufficient for this project's dependency volume at this stage. |

None of these are rejected permanently. They can be reconsidered if the project's scale or requirements change.

---

## 20. Maintenance rules

- Every tool must have documentation explaining what it does and how to use it.
- Every tool must have a clear purpose. No tool is added "just in case."
- Every tool must be optional unless it is required for the application to run.
- Avoid adding a tool without a concrete use case that affects daily development or operations.
- Dashboards and alerts should answer real operational questions, not display data for its own sake.
- Keep secrets out of Git. Use the existing env-file split for any configuration that tools require.
- When the implementation of a tool differs from this document, update the documentation.
