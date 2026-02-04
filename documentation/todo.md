# TODO: `zalduaxa.net` (Portfolio Web)
[Back to menu](/README.md)

This is the current implementation checklist for the portfolio platform (backend + frontend + database + deploy).

## Status legend
- [x] Done (verified in current code)
- [x*] Done (not verified)
- [ ] Pending
- [ ] Pending (nice-to-have)

---

## Backend (API)

### Auth + Sessions
- [x] Signup: `POST /auth/signup` (validations + password hashing)
- [x] Login: `POST /auth/login` (creates DB session + issues JWT + sets HttpOnly cookie)
- [x] Logout: `POST /auth/logout` (deletes DB session + clears cookie)
- [x] Session check: `GET /auth/session` (token -> user + requires active session)
- [ ] Unify error responses (same JSON shape everywhere)
- [ ] Improve login/signup error output (frontend-friendly messages + consistent status codes)
- [ ] Move JWT secret to `application.properties` / env var (no hardcoded secret in code)
- [ ] Review cookie config for production (`Secure`, `SameSite`, domain, expiration)
- [ ] Add rate limiting / brute-force protection (optional but recommended)

### Roles + Permissions
- [x] Role model exists and is used for admin checks in some endpoints
- [ ] Assign a default role on signup (for example `guest`) and persist it
- [ ] Endpoint to list roles: `GET /roles` (admin / internal use)
- [ ] Centralize authorization (instead of repeating checks per controller) using Spring Security

### Project Types
- [x] List project types: `GET /project/projectTypes`
- [x] Create project type (multipart + icon): `POST /project/addProjectType` (admin only)
- [x] Delete project type: `POST /project/deleteProjectType` (admin only)
- [ ] Store and expose the icon path in DB (or define a deterministic convention and document it)
- [ ] Replace the current “delete by name” with “delete by id/slug” (safer)

### Projects
- [x] List projects by project type slug: `GET /project/{slug}/projects`
- [ ] Apply visibility rules in project listing:
  - public (no auth): only public projects
  - guest (auth): public + private
  - admin: full access + CRUD
- [ ] Project detail endpoint:
  - `GET /projects/{id}` or `GET /projects/{typeSlug}/{projectSlug}`
- [ ] Create project (admin): `POST /projects`
- [ ] Update project (admin): `PUT /projects/{id}`
- [ ] Delete project (admin): `DELETE /projects/{id}` (soft delete if you keep `deleted_at`)
- [ ] Consider a clean DTO layer (Request/Response objects) for projects (avoid leaking DB entity shapes)

### Storage / Files
- [x] Backend serves `/storage/**` using `storage.path` from `application.properties`
- [ ] Remove hardcoded filesystem paths in controllers (use `storage.path` or DB `storage.base_path`)
- [ ] Use the `storage` table properly:
  - load base path from DB (or keep it in properties if you want ultra-simple)
  - generate project paths from storage + type slug + project slug
- [ ] Optional: add a small endpoint for storage info: `GET /storage` (admin)

### Error handling / Clean architecture
- [ ] Use `@ControllerAdvice` to map exceptions to consistent responses
- [ ] Remove broad `try/catch` returning unrelated payloads (example: returning projectTypes on error)
- [ ] Add bean validation annotations (`@NotBlank`, `@Email`, etc.) on request DTOs
- [ ] Add logging for errors (without leaking secrets)

---

## Frontend (React)

### Current UI (verified)
- [x] Routes exist: `/projects`, `/projects/:typeSlug`, `/signup`, `/login`, `/logout`
- [x] Project types page lists project types
- [x] Admin can add/delete project types from the UI
- [x] Project type page lists projects of that type

### Missing UI for “portfolio MVP”
- [ ] Project detail page (auto-generated):
  - route idea: `/projects/:typeSlug/:projectSlug`
  - template-based page (title, description, images, links, tags, version)
- [ ] Admin UI for projects:
  - create/edit/delete project
  - upload/manage project assets (if you want it inside the web)
- [ ] Better loading + error UX (consistent messages, maybe a shared component)
- [ ] Auth UX:
  - show current session state in header
  - protected admin routes/components
  - redirect after login/logout

### Portfolio personal info
- [ ] Decide where to store “about me” content:
  - DB tables (editable from admin panel)
  - a single JSON blob in DB
  - plain TSX/MD content in the repo
- [ ] Implement whichever option you choose (and document it)

---

## Database (PostgreSQL)

### Schema / Seeds
- [x] Schema exists under `zalduaxanet` and includes roles/users/storage/projects/types/visibility/status/etc.
- [x] Seed values exist (roles, admin/guest users, default storage, lookup tables)

### Fixes and alignment
- [ ] Fix the seed check for project_type (your example checks `name = 'minecraft-mods'` but it should check `slug = 'minecraft-mods'`)
- [ ] Ensure `project_type.slug` is NOT NULL and UNIQUE (you already have UNIQUE; add NOT NULL if desired)
- [ ] Align DB columns with backend needs:
  - project icon path / file conventions
  - project type icon path (optional)
- [ ] Add indexes for performance and cleanliness:
  - `project.slug`, `project.type_id`, `project.visibility_id`, `project.status_id`
  - `project_type.slug`
- [ ] Decide and document soft-delete policy (`deleted_at`) and ensure queries exclude deleted rows by default

---

## Deployment + Security (Synology NAS target)

- [ ] Decide exposure model:
  - public reverse proxy (recommended: only 80/443 open) OR
  - VPN-only access (more private, but not a public portfolio)
- [ ] Reverse proxy config (route frontend + `/api` + `/storage`)
- [ ] HTTPS certificates (Let’s Encrypt) + forced HTTPS
- [ ] NAS hardening:
  - firewall rules
  - disable unnecessary services
  - backups for DB + storage files
- [ ] Separate secrets from repo (env vars): DB password, JWT secret, admin seed password

---

## Documentation

- [x] README improved
- [x] Database schema doc updated
- [ ] Add “Quickstart” (one-page): prerequisites + run commands + URLs
- [ ] Document API endpoints (Swagger/OpenAPI if you want it clean)
- [ ] Document storage conventions (where icons/assets live and how URLs are built)

---

## Future expansions (optional)
- [ ] Recipes / Drawings / Gallery pages (template-based content)
- [ ] Comments / Likes (content_item + comment + like)
- [ ] Audit log viewer (admin)
