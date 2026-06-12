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

## Next task

**Task 15 — Analyze monitoring/analytics/quality tools**

---

## Phase 2 — Observability, quality, analytics & usability

- [ ] Task 15 — Analyze monitoring/analytics/quality tools
- [ ] Task 16 — Define observability architecture
- [ ] Task 17 — Backend metrics with Spring Boot Actuator
- [ ] Task 18 — Container/host monitoring
- [ ] Task 19 — Decide Grafana stack vs Zabbix
- [ ] Task 20 — Log monitoring
- [ ] Task 21 — SonarQube code quality
- [ ] Task 22 — Frontend analytics
- [ ] Task 23 — Accessibility/usability checks
- [ ] Task 24 — Alerting strategy
- [ ] Task 25 — Monitoring Docker Compose overlay
- [ ] Task 26 — Dashboards and monitoring docs

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
