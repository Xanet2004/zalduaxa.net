# Roadmap — zalduaxa.net

[Back to menu](/README.md)

This document tracks the implementation roadmap for the portfolio platform (backend + frontend + database + deploy).

---

## Phase 1 — Backend Foundation (completed)

- [x] Task 1  — Fix critical backend bugs
- [x] Task 2  — Move secrets/config to env
- [x] Task 3  — Add minimal auth/JWT tests
- [x] Task 4  — Add Bean Validation
- [x] Task 5  — Extract SlugUtils / cleanup
- [x] Task 6  — Extract SessionService
- [x] Task 7  — Extract ProjectService / ProjectTypeService
- [x] Task 8  — Extract StorageService / assets
- [x] Task 9  — Replace custom hashing with BCrypt + pepper
- [x] Task 10 — Introduce Spring Security
- [x] Task 11 — Create dev/prod profiles
- [x] Task 12 — Introduce Flyway
- [x] Task 12.5 — Document Flyway workflow
- [x] Task 13 — Normalize API endpoints
- [x] Task 14 — Reorganize packages into feature-domain structure
- [x] Task 14.5 — Full documentation cleanup

---

## Phase 2 — Observability, quality, analytics & usability

The selected tooling stack is defined in [documentation/tooling.md](tooling.md).

- [ ] Backend metrics with Spring Boot Actuator and Micrometer
- [ ] Prometheus and Grafana dashboards
- [ ] Centralized logging with Loki and Alloy
- [ ] Uptime monitoring with Uptime Kuma
- [ ] Web analytics with Matomo
- [x] SonarQube and JaCoCo code quality
- [x] Local SonarQube setup (`docker-compose.quality.yml`)
- [x] JaCoCo backend coverage (`jacoco-maven-plugin`, `verify` phase)
- [x] Homepage local tools dashboard (`docker-compose.tools.yml`)
- [ ] Integrate SonarQube with CI after deciding self-hosted runner or secure network exposure
- [ ] Quality Gate enforcement
- [ ] Frontend test coverage
- [x] Trivy repository filesystem scanning (vulnerabilities, secrets, misconfigurations)
- [x] SARIF report upload to GitHub Code Scanning
- [ ] Docker image scanning with Trivy
- [ ] Decide whether HIGH severity should block pull requests
- [ ] Add `.trivyignore` only if real false positives appear
- [ ] End-to-end tests with Playwright
- [ ] Automated accessibility checks with axe-core
- [ ] Performance audits with Lighthouse CI
- [ ] API documentation with springdoc-openapi
- [ ] Monitoring Docker Compose overlay files (Prometheus + Grafana, Loki + Alloy, Uptime Kuma)
- [ ] Alerting strategy

---

## Phase 3 — Product features

- [ ] Project update endpoint (`PUT /projects/{slug}`)
- [ ] Remove deprecated `/project/...` endpoints (after frontend migration is stable)
- [ ] OpenAPI / Swagger documentation
- [ ] Recipes (template-based content type)
- [ ] Drawings (template-based content type)
- [ ] Comments / likes on content items
- [ ] Collaborator system
- [ ] Tags / categories
- [ ] Admin dashboard
- [ ] Rate limiting / brute-force protection
- [ ] Review cookie config for production (`Secure`, `SameSite`, domain, expiration)

---

## Deployment

- [ ] Decide exposure model (public reverse proxy vs VPN-only)
- [ ] Reverse proxy config (route frontend + `/api` + `/storage`)
- [ ] HTTPS certificates (Let's Encrypt) + forced HTTPS
- [ ] NAS hardening (firewall, disable unnecessary services, backups)
- [ ] Back up DB + storage files regularly
