# Flyway Database Migrations

[Back to menu](/README.md)

> See also: [Docker setup](docker.md) | [Database schema](db.md)

## Purpose

Flyway version-controls the database schema. It runs SQL migrations in order and records which migrations have been executed in the `zalduaxanet.flyway_schema_history` table. Hibernate no longer creates or updates tables automatically — it now validates schema compatibility with JPA entities using `spring.jpa.hibernate.ddl-auto=validate`.

## Current project setup

- Migration location: `backend/src/main/resources/db/migration/`
- Initial migration: `V1__init_schema.sql`
- Active schema: `zalduaxanet`
- Flyway history table: `zalduaxanet.flyway_schema_history`
- Old schema file: `db/init/00-create-schema.sql` (historical reference only, not executed by Docker)

The old `db/init/00-create-schema.sql` file is historical reference only. Docker no longer executes that file. When the backend starts, Flyway runs first, then Hibernate validates.

## Migration naming rules

Flyway expects this naming pattern:

```
V<version>__<description>.sql
```

Examples:

```
V1__init_schema.sql
V2__add_user_bio.sql
V3__create_tag_table.sql
V4__add_project_cover_image.sql
```

Rules:

- Prefix with `V`.
- Use a sequential number.
- Use double underscore `__` between version and description.
- Use lowercase, underscore-separated descriptions.
- Do not use spaces in the filename.

## Golden rule: do not edit old migrations

**Never edit an old migration after it has run in any shared or production-like environment.**

Flyway stores a checksum of each migration file. If a file changes after it has been applied, Flyway will fail with a checksum mismatch error. New database changes must always go into new migration files: `V2`, `V3`, `V4`, etc.

**Dev exception:** In local development, if the database has no valuable data, you can reset everything:

```bash
docker compose down -v
docker compose up --build
```

This destroys the Postgres volume and reruns all migrations from scratch. **Never do this in production.**

For the dev-only PostgreSQL container:

```bash
docker compose -f docker-compose.dev.yml down -v
docker compose -f docker-compose.dev.yml up -d
```

## How to add a new table

1. Create a new migration file, for example `V2__create_tag_table.sql`.
2. Write the `CREATE TABLE` statement with the `zalduaxanet` schema prefix.
3. Add the JPA entity and repository if needed.
4. Restart the backend. Flyway applies the migration automatically. Hibernate validates the entity matches the schema.

Example:

```sql
CREATE TABLE zalduaxanet.tag (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE
);
```

## How to add a new column

1. Create a new migration, for example `V2__add_user_bio.sql`.
2. Write the `ALTER TABLE` statement.
3. Update the JPA entity (`User.java`) if the backend needs the new field.
4. Restart the backend.

Example:

```sql
ALTER TABLE zalduaxanet."user"
ADD COLUMN bio TEXT;
```

If the entity is updated without the corresponding migration, Hibernate validation will fail on startup.

## How to add lookup data

There are three categories of data:

| Category | Where it goes | Examples |
|----------|--------------|----------|
| Structural lookup data | Flyway migration | roles, visibility values, status values, resource types |
| Dev seed users | `AuthService.defaultUsers()` (Java) | admin, guest |
| Example/test data | Manual SQL in `db/tests/` | not run automatically |

For structural lookup data, add `INSERT` statements to a new migration:

```sql
INSERT INTO zalduaxanet.role (name, description, created_at)
VALUES ('moderator', 'Moderates content', CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;
```

Use `ON CONFLICT` where appropriate for safe re-runnable inserts.

## Development reset workflow

To reset the development database and rerun all migrations:

```bash
docker compose down -v
docker compose up --build
```

- `down -v` deletes the Postgres Docker volume.
- All data is removed.
- Flyway reruns all migrations from scratch.
- This is only safe in development.

To reset only the dev PostgreSQL container (when using `docker-compose.dev.yml`):

```bash
docker compose -f docker-compose.dev.yml down -v
docker compose -f docker-compose.dev.yml up -d
```

## Inspecting migration history

```bash
docker compose exec postgres psql -U app -d zalduaxa_net -c \
'SELECT version, description, success FROM zalduaxanet.flyway_schema_history;'
```

Expected result: `V1__init_schema.sql` appears as version `1`, description `init schema`, success `true`.

## Listing database tables

```bash
docker compose exec postgres psql -U app -d zalduaxa_net -c \
'\dt zalduaxanet.*'
```

Important tables: `flyway_schema_history`, `user`, `role`, `visibility`, `project_type`, `project`, `session`.

## Hibernate validation errors

Hibernate now uses `ddl-auto=validate`. It checks that columns expected by JPA entities exist in the database, but it does not create missing tables or columns automatically.

If the backend fails to start with a validation error, the most likely cause is a mismatch between entities and the database schema. For example: you added a field in `User.java` but forgot to create the corresponding Flyway migration.

**Fix:** Create a new Flyway migration (e.g., `V2__add_missing_column.sql`) to add the missing column or table.

**Do not** switch to `ddl-auto=update` as a normal fix. It can be used as a temporary local workaround, but never commit that change.

## Profile note

Flyway is enabled in both `dev` and `prod` profiles. The active profile is selected via `SPRING_PROFILES_ACTIVE` (defaults to `dev`).

## Local backend workflow

When running the backend outside Docker during development:

```bash
docker compose -f docker-compose.dev.yml up -d
cd backend
mvn spring-boot:run
```

- Postgres runs in a Docker container.
- The backend runs locally with `mvn spring-boot:run`.
- Flyway still runs during backend startup and applies migrations to the local database.

## Production notes

- Flyway runs automatically on production startup.
- `baseline-on-migrate=false` is intentional — fresh production databases are expected to run migrations normally.
- Existing production databases with real data may require a one-time baseline strategy later if Flyway is introduced into an existing environment.
- **Never** run `docker compose down -v` in production unless you intend to delete all data.
- Always back up the production database before applying schema migrations.

## Quick command reference

```bash
# Run tests
cd backend
mvn clean test
```

```bash
# Start full stack
docker compose up --build
```

```bash
# Reset dev DB and rerun all migrations
docker compose down -v
docker compose up --build
```

```bash
# Check Flyway logs
docker compose logs backend | grep -i flyway
```

```bash
# Check active Spring profile
docker compose logs backend | grep -i profile
```

```bash
# Check Flyway migration history
docker compose exec postgres psql -U app -d zalduaxa_net -c \
'SELECT version, description, success FROM zalduaxanet.flyway_schema_history;'
```

```bash
# List schema tables
docker compose exec postgres psql -U app -d zalduaxa_net -c \
'\dt zalduaxanet.*'
```
