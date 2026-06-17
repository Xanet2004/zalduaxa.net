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

## Phase 2 — Observability, quality, analytics & usability (completed)

The selected tooling stack is defined in [documentation/tooling.md](tooling.md).

- [x] 15. Decide and document Phase 2 tool stack
- [x] 16. Add GitHub Actions CI
- [x] 17. Add Dependabot
- [x] 18.1 Add SonarQube + JaCoCo
- [x] 18.2 Homepage dashboard
- [x] 19. Add Trivy
- [x] 20. Add Spring Boot Actuator
- [x] 21.1 Prometheus
- [x] 21.2 Grafana
- [x] 22. Add Loki + Alloy
- [x] 23. Add Uptime Kuma
- [x] 24. Add Matomo analytics
- [x] 25.1 Playwright basic smoke tests
- [x] 25.2 axe accessibility tests
- [x] 25.3 Lighthouse local metrics
- [x] 26. Add OpenAPI / Swagger
- [x] 27. Document Phase 2 workflows

### Phase 2 follow-up tasks

- [ ] Fix frontend accessibility issues detected by quality-runner
- [ ] Add CI integration for quality-runner (Playwright in GitHub Actions)
- [ ] Improve frontend quality Grafana dashboard
- [ ] Configure Uptime Kuma notifications (email, Telegram, etc.)
- [ ] Review Matomo privacy/cookie consent for production
- [ ] Add production hardening for Swagger/observability endpoints
- [ ] Integrate SonarQube with CI (self-hosted runner or secure network exposure)
- [ ] Enable Docker image scanning with Trivy
- [ ] Decide whether HIGH severity should block PRs
- [ ] Add `.trivyignore` if real false positives appear

---

## Phase 3 — Product features

- [ ] Project update endpoint (`PUT /projects/{slug}`)
- [ ] Remove deprecated `/project/...` endpoints (after frontend migration is stable)
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
