# Database Schema: `zalduaxanet` (PostgreSQL)
[Back to menu](/README.md)

This document describes the current PostgreSQL schema used by the project.  
All objects live inside the `zalduaxanet` schema (`SET search_path TO zalduaxanet;`).

## Lookup Tables

| Table | Fields | Description |
|------|--------|-------------|
| **visibility** | `id` (PK), `code` (UNIQUE), `name` | Resource visibility (public/private) |
| **status** | `id` (PK), `code` (UNIQUE), `name` | Publishing status (draft/published) |
| **collaborator_role** | `id` (PK), `code` (UNIQUE), `name` | Collaborator roles (editor/viewer) |
| **resource_type** | `id` (PK), `code` (UNIQUE), `name` | Content types (project/recipe/drawing) |

---

## Core Tables

### `role`
| Field | Type | Description |
|------|------|-------------|
| `id` (PK) | SERIAL | Unique identifier |
| `name` | VARCHAR(255) | Role name (admin/member/guest) |
| `description` | VARCHAR(255) | Description |
| `created_at` | TIMESTAMP | Creation date |

### `user`
Note: the table name is quoted because `user` is reserved in PostgreSQL.

| Field | Type | Relation / Notes |
|------|------|------------------|
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

### `session`
Tracks login sessions/tokens.

| Field | Type | Relation / Notes |
|------|------|------------------|
| `id` (PK) | SERIAL | |
| `user_id` | INT | FK → `"user".id` (ON DELETE CASCADE) |
| `token` | VARCHAR(255) | UNIQUE, NOT NULL |
| `ip_address` | VARCHAR(100) | |
| `user_agent` | VARCHAR(500) | |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| `expires_at` | TIMESTAMP | |
| `last_activity` | TIMESTAMP | |

### `storage`
Represents storage backends (local bucket/path, future providers, etc.).

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

### `project_type`
Project categorization.

| Field | Type | Notes |
|------|------|------|
| `id` (PK) | SERIAL | |
| `name` | VARCHAR(255) | |
| `description` | VARCHAR(255) | |
| `slug` | VARCHAR(255) | UNIQUE |

### `project`
Main portfolio entity.

| Field | Type | Relation / Notes |
|------|------|------------------|
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
| `UNIQUE(storage_id, slug)` | | Prevents slug collisions within the same storage |

### `project_collaborator`
Many-to-many between projects and users with a collaborator role.

| Field | Type | Relation / Notes |
|------|------|------------------|
| `project_id` | INT | FK → `project.id` (ON DELETE CASCADE) |
| `user_id` | INT | FK → `"user".id` (ON DELETE CASCADE) |
| `role_id` | INT | FK → `collaborator_role.id` |
| `invited_by` | INT | FK → `"user".id` |
| `accepted_at` | TIMESTAMP | |
| `PRIMARY KEY (project_id, user_id)` | | |

### `recipe`
Example additional resource type.

| Field | Type | Relation / Notes |
|------|------|------------------|
| `id` (PK) | SERIAL | |
| `storage_id` | INT | FK → `storage.id` |
| `created_by` | INT | FK → `"user".id` |
| `title` | VARCHAR(255) | |
| `description` | TEXT | |
| `path` | VARCHAR(255) | Storage path |
| `visibility_id` | INT | FK → `visibility.id` |
| `version` | VARCHAR(255) | |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |
| `deleted_at` | TIMESTAMP | Soft delete |

### `drawing`
Example additional resource type.

| Field | Type | Relation / Notes |
|------|------|------------------|
| `id` (PK) | SERIAL | |
| `storage_id` | INT | FK → `storage.id` |
| `created_by` | INT | FK → `"user".id` |
| `title` | VARCHAR(255) | |
| `description` | TEXT | |
| `path` | VARCHAR(255) | Storage path |
| `visibility_id` | INT | FK → `visibility.id` |
| `version` | VARCHAR(255) | |
| `created_at` | TIMESTAMP | |
| `updated_at` | TIMESTAMP | |
| `deleted_at` | TIMESTAMP | Soft delete |

### `content_item`
Generic wrapper for different resource types, allowing cross-resource comments/likes.

| Field | Type | Relation / Notes |
|------|------|------------------|
| `id` (PK) | SERIAL | |
| `resource_type_id` | INT | FK → `resource_type.id` |
| `resource_id` | INT | ID of the target row in its table |
| `created_by` | INT | FK → `"user".id` |
| `created_at` | TIMESTAMP | |

### `comment`
Comments attached to `content_item`.

| Field | Type | Relation / Notes |
|------|------|------------------|
| `id` (PK) | SERIAL | |
| `content_item_id` | INT | FK → `content_item.id` (ON DELETE CASCADE) |
| `user_id` | INT | FK → `"user".id` |
| `text` | TEXT | |
| `created_at` | TIMESTAMP | |
| `deleted_at` | TIMESTAMP | Soft delete |

### `like`
Likes attached to `content_item`.
Note: the table name is quoted because `like` is reserved in SQL.

| Field | Type | Relation / Notes |
|------|------|------------------|
| `id` (PK) | SERIAL | |
| `content_item_id` | INT | FK → `content_item.id` (ON DELETE CASCADE) |
| `user_id` | INT | FK → `"user".id` |
| `created_at` | TIMESTAMP | |
| `UNIQUE(content_item_id, user_id)` | | One like per user per content item |

### `audit_log`
Tracks changes across entities.

| Field | Type | Relation / Notes |
|------|------|------------------|
| `id` (PK) | SERIAL | |
| `entity_type` | VARCHAR(255) | Entity/table name |
| `entity_id` | INT | Row id |
| `action` | VARCHAR(255) | Action performed |
| `performed_by` | INT | FK → `"user".id` |
| `changes` | JSON | JSON payload with changes |
| `created_at` | TIMESTAMP | |

---

## Seeds (default values)

The schema includes optional seed inserts for:

- `visibility`: public, private
- `status`: draft, published
- `collaborator_role`: editor, viewer
- `resource_type`: project, recipe, drawing
- `role`: admin, member, guest
- default users: `admin`, `guest`
- default storage: `local-storage`

---

## Insert examples

### Insert a `project_type` if it does not exist
```sql
INSERT INTO zalduaxanet.project_type (name, description, slug)
SELECT
  'Minecraft Mods',
  'Minecraft mods created by myself!',
  'minecraft-mods'
WHERE NOT EXISTS (
  SELECT 1 FROM zalduaxanet.project_type WHERE slug = 'minecraft-mods'
);
