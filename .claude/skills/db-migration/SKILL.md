---
name: db-migration
description: Change the SourceFolio database schema through a Flyway migration and keep the JPA mapping in step. Use whenever a table, column or index is added, changed or removed, when an entity needs a new column, or when facing a Hibernate "schema validation" error, a "column/relation does not exist" error or a Flyway "checksum mismatch".
---

# Schema migration (Flyway)

## The core rule

**Flyway owns the schema in every environment.** Hibernate runs with `ddl-auto: validate`: it
never creates or alters a table, it only refuses to start when an entity and the schema
disagree. What proves that queries and schema line up is the adapter's `*IT`, run by
`./mvnw verify` against PostgreSQL in Testcontainers (CI runs it on every PR).

JobRunr manages its own `jobrunr_*` tables; they are the one exception, and no migration of
ours touches them.

## Procedure

1. Look up the highest number in `backend/src/main/resources/db/migration/`.
2. Create `V{n+1}__short_description.sql` (two underscores after the number, snake_case).
3. Write the DDL. The SQL dialect is PostgreSQL.
4. Index what the queries need: every new foreign key, and every column a frequent query
   filters, joins or sorts on, in the same migration (`CREATE INDEX … ON table (col)`).
   Use the shapes of the design-patterns skill: `TIMESTAMPTZ`, `VARCHAR(1000)` URLs,
   `status VARCHAR(20)` with a `CHECK`, `CHAR(2)` country codes, UUID primary keys.
5. Update the `XxxEntity`, the mapping in `JpaXxxStore`, and the `*IT` covering every query.

Example:
```sql
-- V7__add_showcase_check_screenshots.sql
ALTER TABLE showcase_checks
    ADD COLUMN screenshot_mobile_key  VARCHAR(500),
    ADD COLUMN screenshot_desktop_key VARCHAR(500);
```

## Hard limits

- **Never edit a migration that is already merged into `develop` or `main`.** Flyway
  stores a checksum; any edit breaks startup on every database where it has been
  applied, production included. Fixing something means adding the next migration.
- Never renumber or delete an existing migration.
- Renaming a table or column: keep ids stable (`ALTER TABLE … RENAME`), so foreign keys survive.
- Business limits stay in the rules, not the schema: one published showcase per member is a
  partial unique index at most (`… WHERE status = 'PUBLISHED'`), never a one-to-one table.

## Project specifics

- No baseline: V1 creates the database from nothing.
- For a **NOT NULL column added to a non-empty table**, do it in three steps inside
  the same migration: add the column nullable, backfill it (`UPDATE`), then `SET NOT NULL`.
- Reference data (specialties, technologies with aliases, FR/EN labels) is inserted by
  migrations so every environment has the same slugs; a label change is a new migration.
- Statuses with a `CHECK`: `showcases.status IN ('DRAFT','PENDING_REVIEW','PUBLISHED',
  'UNAVAILABLE','NEEDS_FIX','REMOVED','ARCHIVED')`. Adding a status means replacing the
  constraint in a new migration.

## Starting over (development only)
```
docker compose down -v && docker compose up -d
```
`-v` destroys the volumes and therefore the local data. Never in production.
