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

Later stages can add SonarQube analysis, Trivy scans, Playwright tests, Lighthouse CI reports, and Docker build validation.

Workflows will be defined incrementally as each tool is introduced. No workflow files exist yet.

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
- Actuator endpoints are not enabled in the codebase yet. They will be added and secured as a specific implementation step.

---

## 9. Metrics and dashboards

**Selected tools: Prometheus, Grafana.**

Prometheus scrapes metrics from the backend Actuator endpoint and stores them as time-series data. Grafana connects to Prometheus as a data source and provides visual dashboards.

Initial dashboard panels will cover:

- JVM heap and non-heap memory usage.
- CPU and thread activity.
- HTTP request throughput and latency percentiles.
- HTTP 4xx and 5xx error rates.
- Backend uptime.
- Database connection pool health.
- Container-level resource metrics (added later with cAdvisor or similar).

---

## 10. Centralized logging

**Selected tools: Grafana Loki, Grafana Alloy.**

Loki stores and indexes log data without the overhead of a full-text search engine like Elasticsearch. It integrates directly with Grafana, so logs appear alongside metrics dashboards.

Alloy collects logs from each service and forwards them to Loki. It can also serve as a telemetry pipeline for traces and metrics in the future. Services to collect logs from:

- Backend Spring Boot (stdout or file‑based).
- Frontend nginx (access and error logs).
- PostgreSQL (slow queries and general logs).
- Other Docker containers as the stack grows.

This avoids relying solely on `docker logs` for debugging, which is impractical once multiple services are running.

---

## 11. Uptime monitoring

**Selected tool: Uptime Kuma.**

Uptime Kuma periodically checks whether public endpoints are reachable. It provides a status dashboard, configurable check intervals, and alerting.

Endpoints to monitor:

- Public home page.
- Public project types API.
- Backend health endpoint (once available).
- Storage route.

Alerts can later be routed to email, Discord, Telegram, or push notifications.

---

## 12. Web analytics

**Selected tool: Matomo.**

Matomo is a privacy-friendly, self-hosted analytics platform. It tracks visitor statistics without sending data to third parties.

Metrics collected:

- Page views, unique visitors, sessions.
- Visit duration and bounce rate.
- Referrers and search terms.
- Device, browser, and operating system.
- Country and language.
- Specific project and project-type page views.

Implementation notes:

- Matomo must not track authenticated backend requests or session data.
- A privacy/cookie consent mechanism will be needed before deployment.
- Matomo runs as its own Docker Compose service with a MariaDB/MySQL database.

---

## 13. Frontend quality assurance

**Selected tools: Playwright, axe-core, Lighthouse CI.**

| Tool | Role |
|------|------|
| Playwright | End-to-end browser tests covering real user flows |
| axe-core | Automated accessibility audits integrated into Playwright tests |
| Lighthouse CI | Performance, accessibility, SEO, and best-practice scoring |

Initial test flows:

- Home page loads and renders project types.
- Project type page shows projects for a given slug.
- Project detail page loads content.
- Login and logout round-trip.
- Profile page for authenticated users.
- 404 page for unknown routes.

These tests will run in CI and provide a safety net before each deployment.

---

## 14. API documentation

**Selected tool: springdoc-openapi.**

springdoc-openapi generates OpenAPI 3 documentation directly from Spring Boot controller annotations, request DTOs, and response types. This keeps the API documentation in sync with the code and eliminates stale hand-written endpoint docs.

Expected routes once enabled:

| Path | Purpose |
|------|---------|
| `/v3/api-docs` | OpenAPI JSON schema |
| `/swagger-ui/index.html` | Interactive API explorer |

Access to these routes should be protected, disabled in production, or limited to the `dev` profile.

---

## 15. Runtime architecture

The following diagram shows the target tooling architecture. Not every component is running yet — this is the selected stack that will be built incrementally.

```
GitHub
├── GitHub Actions
├── Dependabot
└── Repository checks

Docker runtime
├── frontend nginx
├── backend Spring Boot
│   ├── Actuator
│   ├── Prometheus metrics
│   └── OpenAPI docs
├── PostgreSQL
├── Prometheus
├── Grafana
├── Loki
├── Alloy
├── Uptime Kuma
├── Matomo
└── SonarQube
```

---

## 16. Docker Compose strategy

Each group of tools will live in its own Compose file so the base application stack stays clean:

| File | Contents |
|------|----------|
| `docker-compose.yml` | Base application: postgres, backend, frontend |
| `docker-compose.monitoring.yml` | Prometheus, Grafana, Loki, Alloy, Uptime Kuma |
| `docker-compose.quality.yml` | SonarQube + PostgreSQL 16 (dedicated) |
| `docker-compose.tools.yml` | Homepage local tools dashboard |
| `docker-compose.analytics.yml` | Matomo |

`docker-compose.quality.yml` and `docker-compose.tools.yml` are implemented. The others will be created as each tool is introduced.

Tooling compose files extend the base project where needed and can be started independently:

```bash
docker compose -f docker-compose.monitoring.yml up -d
```

This keeps the base `docker compose up --build` fast and focused on the application itself.

---

## 17. Implementation order

Tools will be introduced in this sequence. Each step builds on the previous one:

1. **GitHub Actions CI** — Backend tests and frontend build on every push. Implemented.
2. **Dependabot** — Automated dependency update pull requests. Implemented.
3. **SonarQube + JaCoCo** — Quality dashboard with coverage reporting. Implemented locally; CI integration postponed.
4. **Trivy** — Vulnerability scanning in CI.
5. **Spring Boot Actuator + Micrometer** — Health and metrics endpoints.
6. **Prometheus + Grafana** — Metrics storage and dashboards.
7. **Loki + Alloy** — Centralized log collection.
8. **Uptime Kuma** — External availability monitoring.
9. **Matomo** — Self-hosted web analytics.
10. **Playwright + axe-core + Lighthouse CI** — Frontend quality automation.
11. **springdoc-openapi** — Generated API documentation.
12. **Final documentation** — Monitoring and quality workflow guides.

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
