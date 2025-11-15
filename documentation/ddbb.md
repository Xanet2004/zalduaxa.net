# Database Schema: `zalduaxanetDB`

## Lookup Tables

| Table | Fields | Description |
|-------|--------|-------------|
| **providers** | `id` (PK), `code`, `name` | Storage types (e.g., local, S3, GCS) |
| **visibilities** | `id` (PK), `code`, `name` | Resource visibility (public/private) |
| **statuses** | `id` (PK), `code`, `name` | Project status (draft/published) |
| **collaborator_roles** | `id` (PK), `code`, `name` | Collaborator roles (editor/viewer) |
| **resource_types** | `id` (PK), `code`, `name` | Resource types (project, recipe, drawing) |

---

## Core Tables

### `roles`
| Field | Type | Description |
|-------|------|-------------|
| id (PK) | INT | Unique identifier |
| name | VARCHAR | Role name |
| description | VARCHAR | Description |
| created_at | DATETIME | Creation date |

### `users`
| Field | Type | Relation |
|-------|------|----------|
| id (PK) | INT | |
| role_id | INT | FK → `roles.id` |
| username, full_name, email, phone, profile_picture, linkedin, github, website | VARCHAR | User information |
| is_active | TINYINT | Active or not |
| created_at, updated_at, deleted_at | DATETIME | Management timestamps |

### `storage`
| Field | Type | Description |
|-------|------|-------------|
| id (PK) | INT | |
| name, bucket, base_path, description | VARCHAR | Storage information |
| config | JSON | Specific configuration |
| created_at, updated_at | DATETIME | Management timestamps |

### `project_types`
| Field | Type | Description |
|-------|------|-------------|
| id (PK) | INT | |
| name, description, image_path | VARCHAR | Project type information |

### `projects`
| Field | Type | Relation |
|-------|------|----------|
| id (PK) | INT | |
| storage_id | INT | FK → `storage.id` |
| owner_id | INT | FK → `users.id` |
| type_id | INT | FK → `project_types.id` |
| visibility_id | INT | FK → `visibilities.id` |
| status_id | INT | FK → `statuses.id` |
| name, slug, description, icon_path, path, version | VARCHAR/TEXT | Project information |
| metadata | JSON | Additional data |
| created_at, updated_at, deleted_at | DATETIME | Management timestamps |
| UNIQUE(storage_id, slug) | | Uniqueness constraint |

### `project_collaborators`
| Field | Type | Relation |
|-------|------|----------|
| project_id | INT | FK → `projects.id` |
| user_id | INT | FK → `users.id` |
| role_id | INT | FK → `collaborator_roles.id` |
| invited_by | INT | FK → `users.id` |
| accepted_at | DATETIME | Acceptance date |
| PK (project_id, user_id) | | |

### `recipes` / `drawings` (similar structure)
| Field | Type | Relation |
|-------|------|----------|
| id (PK) | INT | |
| storage_id | INT | FK → `storage.id` |
| created_by | INT | FK → `users.id` |
| title, description, path, version | VARCHAR/TEXT | Resource information |
| visibility_id | INT | FK → `visibilities.id` |
| created_at, updated_at, deleted_at | DATETIME | Management timestamps |

### `content_items`
| Field | Type | Relation |
|-------|------|----------|
| id (PK) | INT | |
| resource_type_id | INT | FK → `resource_types.id` |
| resource_id | INT | Specific resource ID |
| created_by | INT | FK → `users.id` |
| created_at | DATETIME | Creation timestamp |

### `comments`
| Field | Type | Relation |
|-------|------|----------|
| id (PK) | INT | |
| content_item_id | INT | FK → `content_items.id` |
| user_id | INT | FK → `users.id` |
| text | TEXT | Comment text |
| created_at, deleted_at | DATETIME | Management timestamps |

### `likes`
| Field | Type | Relation |
|-------|------|----------|
| id (PK) | INT | |
| content_item_id | INT | FK → `content_items.id` |
| user_id | INT | FK → `users.id` |
| created_at | DATETIME | Timestamp of the "like" |
| UNIQUE(content_item_id, user_id) | | |

### `audit_logs`
| Field | Type | Relation |
|-------|------|----------|
| id (PK) | INT | |
| entity_type | VARCHAR | Type of audited entity |
| entity_id | INT | Entity ID |
| action | VARCHAR | Performed action |
| performed_by | INT | FK → `users.id` |
| changes | JSON | Changes performed |
| created_at | DATETIME | Audit timestamp |
