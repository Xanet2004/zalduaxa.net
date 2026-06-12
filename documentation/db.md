# Database Schema

[Back to menu](/README.md)

> **Important:** The active database schema is managed by **Flyway** from `backend/src/main/resources/db/migration/V1__init_schema.sql`. The old `db/init/00-create-schema.sql` file is historical/reference only and is not mounted by Docker.

## Technology

- **Database**: PostgreSQL
- **Schema name**: `zalduaxanet`
- **Migration tool**: Flyway (runs on backend startup)
- **Flyway history table**: `zalduaxanet.flyway_schema_history`
- **Hibernate mode**: `ddl-auto=validate` (validates entity/schema compatibility, does not create tables)
- **Seed users**: Created by Java code (`AuthService.defaultUsers()` with `@PostConstruct`), not by SQL

## Schema tables

All objects live inside the `zalduaxanet` schema (`SET search_path TO zalduaxanet;`).

### Lookup tables

| Table | Fields | Description |
|------|--------|-------------|
| `visibility` | `id` (PK), `name`, `description` | Resource visibility (public/private) |
| `status` | `id` (PK), `code` (UNIQUE), `name` | Publishing status (draft/published) |
| `collaborator_role` | `id` (PK), `code` (UNIQUE), `name` | Collaborator roles (editor/viewer) |
| `resource_type` | `id` (PK), `code` (UNIQUE), `name` | Content types (project/recipe/drawing) |

### Core tables

#### `role`

| Field | Type | Description |
|------|------|-------------|
| `id` (PK) | SERIAL | Unique identifier |
| `name` | VARCHAR(255) | Role name (admin/member/guest) |
| `description` | VARCHAR(255) | Description |
| `created_at` | TIMESTAMP | Creation date |

#### `"user"`

Note: the table name is quoted because `user` is reserved in PostgreSQL.

| Field | Type | Notes |
|------|------|-------|
| `id` (PK) | SERIAL | |
| `username` | VARCHAR(255) | UNIQUE |
| `full_name` | VARCHAR(255) | |
| `email` | VARCHAR(255) | UNIQUE |
| `password_hash` | VARCHAR(255) | NOT NULL |
| `phone` | VARCHAR(255) | |
| `profile_picture` | VARCHAR(255) | |
| `linkedin` | VARCHAR(255) | |
| `github` | VARCHAR(255) | |
| `website` | VARCHAR(255) | |
| `role_id` | INT | FK → `role.id` |
| `is_active` | BOOLEAN | DEFAULT TRUE |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |
| `deleted_at` | TIMESTAMP | Soft delete |

#### `session`

Tracks login sessions/tokens.

| Field | Type | Notes |
|------|------|-------|
| `id` (PK) | SERIAL | |
| `user_id` | INT | FK → `"user".id` (ON DELETE CASCADE) |
| `token` | VARCHAR(255) | UNIQUE, NOT NULL |
| `ip_address` | VARCHAR(100) | |
| `user_agent` | VARCHAR(500) | |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| `expires_at` | TIMESTAMP | |
| `last_activity` | TIMESTAMP | |

#### `storage`

Represents storage backends (local bucket/path, future providers).

| Field | Type | Description |
|------|------|-------------|
| `id` (PK) | SERIAL | |
| `name` | VARCHAR(255) | Storage name |
| `bucket` | VARCHAR(255) | Bucket/container name (logical) |
| `base_path` | VARCHAR(255) | Base path/prefix |
| `config` | JSON | Provider configuration |
| `description` | VARCHAR(255) | |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |

#### `project_type`

| Field | Type | Notes |
|------|------|-------|
| `id` (PK) | SERIAL | |
| `name` | VARCHAR(255) | |
| `description` | VARCHAR(255) | |
| `slug` | VARCHAR(255) | UNIQUE |
| `visibility_id` | INT | FK → `visibility.id` |

#### `project`

| Field | Type | Notes |
|------|------|-------|
| `id` (PK) | SERIAL | |
| `storage_id` | INT | FK → `storage.id` |
| `owner_id` | INT | FK → `"user".id` |
| `name` | VARCHAR(255) | |
| `slug` | VARCHAR(255) | Used for routing/URLs |
| `type_id` | INT | FK → `project_type.id` |
| `description` | TEXT | |
| `visibility_id` | INT | FK → `visibility.id` |
| `status_id` | INT | FK → `status.id` |
| `version` | VARCHAR(255) | |
| `metadata` | JSON | Additional data (tags, flags, etc.) |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |
| `deleted_at` | TIMESTAMP | Soft delete |
| `UNIQUE(storage_id, slug)` | | Prevents slug collisions within a storage |

#### `project_collaborator`

| Field | Type | Notes |
|------|------|-------|
| `project_id` | INT | FK → `project.id` (ON DELETE CASCADE) |
| `user_id` | INT | FK → `"user".id` (ON DELETE CASCADE) |
| `role_id` | INT | FK → `collaborator_role.id` |
| `invited_by` | INT | FK → `"user".id` |
| `accepted_at` | TIMESTAMP | |
| `PRIMARY KEY (project_id, user_id)` | | |

### Future-use tables

These tables exist in the schema but do not yet have Java entities. They are reserved for future features.

| Table | Future purpose |
|-------|---------------|
| `recipe` | Portfolio recipes |
| `drawing` | Portfolio drawings |
| `content_item` | Generic wrapper for cross-resource comments/likes |
| `comment` | User comments on content items |
| `"like"` | User likes on content items |
| `audit_log` | Audit trail for entity changes |
| `collaborator_role` | Roles within project collaborator system |

## Seed data

The migration inserts initial lookup data:

| Table | Seed values |
|-------|-------------|
| `visibility` | public, private |
| `status` | draft, published |
| `collaborator_role` | editor, viewer |
| `resource_type` | project, recipe, drawing |
| `role` | admin, member, guest |
| `storage` | local-storage (local provider) |

Default users (admin, guest) are created by `AuthService.defaultUsers()` at backend startup — they are not inserted by SQL.

## Inspecting the database

List tables in the schema:

```bash
docker compose exec postgres psql -U app -d zalduaxa_net -c '\dt zalduaxanet.*'
```

Check Flyway migration history:

```bash
docker compose exec postgres psql -U app -d zalduaxa_net -c \
'SELECT version, description, success FROM zalduaxanet.flyway_schema_history;'
```

## Insert example

```sql
INSERT INTO zalduaxanet.project_type (name, description, slug)
SELECT
  'Minecraft Mods',
  'Minecraft mods created by myself!',
  'minecraft-mods'
WHERE NOT EXISTS (
  SELECT 1 FROM zalduaxanet.project_type WHERE slug = 'minecraft-mods'
);
```
