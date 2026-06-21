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

## Phase 3 — Stability, Admin Console & Data Integrity

This phase is NOT about product features (recipes, drawings, comments, likes, collaborators, AI). It is about making the system stronger, cleaner, more governable and more scalable before continuing feature development.

### Core decisions

- **Source of truth:** PostgreSQL is the source of truth for projects. Storage is physical asset storage.
- **Storage root:** Configurable through `STORAGE_PATH`. Should live outside containers (e.g. Synology NAS / host path).
- **Storage layout (future):** `storage/projects/{projectId}/`. Current slug-based layout will be supported during transition.
- **Storage consistency:** Starts read-only. No auto-delete. No auto-import into DB.
- **Audit logs:** Stored in PostgreSQL. Visible only to ROLE_ADMIN.
- **Audit retention:** 6 months default, 12 months maximum.
- **Admin tools:** Links and tables first. No embedded Grafana dashboards initially.
- **No secrets/tokens** in frontend.

### Block A — Audit base

- [ ] 28. Add AuditLog entity and repository

  * Map existing `audit_log` table.
  * Add repository.
  * Add action/entity constants or enums.
  * Add Flyway indexes if needed.

- [ ] 29. Add AuditService

  * Central service for writing safe audit events.
  * Sanitize metadata.
  * Never store passwords, JWTs, cookies, tokens, peppers or secrets.

- [ ] 30. Add audit hooks to important actions

  * LOGIN_SUCCESS / LOGIN_FAILED / LOGOUT
  * USER_CREATED
  * PROJECT_CREATED / PROJECT_DELETED
  * PROJECT_UPDATED (later, when update endpoint exists)
  * PROJECT_TYPE_CREATED / PROJECT_TYPE_DELETED

### Block B — Admin backend

- [ ] 31. Add admin backend package and dashboard endpoint

  * `GET /admin/dashboard` — return safe tool links/config.
  * Protect with ROLE_ADMIN.

- [ ] 32. Add admin audit logs endpoint

  * `GET /admin/audit-logs` — pagination and basic filters.
  * Protect with ROLE_ADMIN.

- [ ] 33. Add admin user management endpoints

  * List users.
  * Enable/disable users.
  * No hard delete.
  * No role editing from frontend initially.

### Block C — Admin frontend

- [ ] 34. Add frontend Administrator dropdown and route guard

  * Show only for admin users.
  * Backend remains the real security layer.

- [ ] 35. Add admin dashboard shell

  * `/admin`, `/admin/tools`, `/admin/audit-logs`, `/admin/users`
  * Simple UI: links and tables.

- [ ] 36. Add audit logs admin page

  * Paginated table.
  * Basic filters if simple.

- [ ] 37. Add users admin page

  * List users.
  * Show role/status.
  * Enable/disable users.

### Block D — Storage consistency

- [ ] 38. Add storage consistency checker in read-only mode

  * Compare PostgreSQL projects with storage.
  * Detect current slug-based layout.
  * Detect future projectId-based layout.
  * No automatic delete.
  * No automatic import.

- [ ] 39. Add storage consistency admin endpoints

  * `GET /admin/storage/reports`
  * `GET /admin/storage/reports/{id}`
  * `POST /admin/storage/scan` — with cooldown/rate limit.

- [ ] 40. Add storage consistency admin page

  * Show last scan.
  * Show issues.
  * Manual scan button.
  * No destructive actions.

- [ ] 41. Add storage consistency metrics

  * `storage_consistency_status`
  * `storage_orphan_folders_total`
  * `storage_missing_project_folders_total`
  * `storage_legacy_layout_folders_total`
  * `storage_last_scan_timestamp_seconds`
  * Avoid path labels in Prometheus.

### Block E — Storage deletion/archive

- [ ] 42. Change project deletion to soft delete + archive storage

  * Use `deleted_at`.
  * Move storage to `.trash/projects/{projectId}/`.
  * Do not hard-delete immediately.
  * Audit PROJECT_DELETED.

- [ ] 43. Add project update endpoint

  * `PUT /projects/{slug}`
  * Audit PROJECT_UPDATED.
  * Do not revive soft-deleted projects through PUT.

- [ ] 44. Migrate storage layout toward projectId

  * New layout: `storage/projects/{projectId}/`.
  * Support legacy layout during transition.
  * Migrate existing files manually or with a safe controlled process.

### Block F — Cleanup and security

- [ ] 45. Remove deprecated endpoints

  * Remove legacy `@Deprecated` endpoints.
  * Remove old SecurityConfig rules.
  * Confirm frontend uses new endpoints.

- [ ] 46. Review production cookie/security config

  * httpOnly, Secure, SameSite, expiration, dev/prod behavior.

- [ ] 47. Add retention cleanup jobs

  * Audit log cleanup with configurable `APP_AUDIT_LOG_RETENTION_DAYS=180`.
  * Trash cleanup later when archive behavior is stable.

- [ ] 48. Add basic rate limiting / cooldowns

  * Login failed attempts.
  * Admin storage scan cooldown.

### Block G — Documentation by area

- [ ] 49. Update documentation by area

  * Do not create `phase-3-plan.md`.
  * Update documentation after each implemented area:

    * `documentation/todo.md`
    * database documentation
    * security documentation
    * Docker documentation
    * project structure documentation
    * tooling/observability documentation if needed
    * README only if needed

### Not in Phase 3 (out of scope)

- Recipes, drawings, comments, likes, collaborators, tags / categories
- AI file analysis
- Complex CSS redesign
- Grafana iframe embedding
- Automatic storage deletion
- Automatic storage import into DB
- Public production exposure of admin/observability tools

---

## Deployment

- [ ] Decide exposure model (public reverse proxy vs VPN-only)
- [ ] Reverse proxy config (route frontend + `/api` + `/storage`)
- [ ] HTTPS certificates (Let's Encrypt) + forced HTTPS
- [ ] NAS hardening (firewall, disable unnecessary services, backups)
- [ ] Back up DB + storage files regularly
