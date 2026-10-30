---
name: dev-setup
description: Start the SourceFolio development environment (PostgreSQL, RustFS for S3, Mailpit, backend, frontend) or work out why it will not start, including GitHub sign-in locally and Playwright screenshots. Use when asked to run the project locally, or when a service refuses to come up.
---

# Running SourceFolio locally

## Start order

1. **Dependencies** (from the repo root):
   ```
   docker compose up -d
   ```
   No `.env` is needed: everything has a development default. Wait for `healthy`:
   ```
   docker compose ps
   ```
   PostgreSQL on 5432, RustFS (S3) on 9000 with its console on 9001 (`rustfsadmin` /
   `rustfsadmin`), Mailpit SMTP on 1025 and its inbox on http://localhost:8025.

2. **Backend** (from `backend/`):
   ```
   ./mvnw spring-boot:run
   ```
   Listens on 8080, `dev` profile. Swagger on `/swagger-ui.html`, JobRunr dashboard on :8000.
   The dev profile creates the screenshots bucket if it is missing. Containerised alternative:
   `docker compose --profile app up -d --build`.

3. **Frontend** (from `frontend/`):
   ```
   pnpm install && pnpm dev
   ```
   Listens on 3000. `pnpm` only — never `npm`. Next.js rewrites `/api`, `/oauth2` and
   `/login/oauth2` to `localhost:8080`, so the browser stays on one origin, as in production.

## GitHub sign-in locally

Create a GitHub OAuth App (Settings → Developer settings → OAuth Apps) with:
- Homepage URL `http://localhost:3000`
- Authorization callback URL `http://localhost:3000/login/oauth2/code/github`

Export its id and secret in the terminal that runs the backend: `./mvnw spring-boot:run`
does not read a `.env` file (only `docker compose` does, for the containerised backend).
```
export GITHUB_CLIENT_ID=... GITHUB_CLIENT_SECRET=...
export GITHUB_API_TOKEN=...   # optional: portfolio detection, 60 anonymous calls an hour without it
cd backend && ./mvnw spring-boot:run
```
The only scope requested is `read:user`. Without these variables the backend starts, and every
public page works; only sign-in fails.

## Playwright screenshots

Running the backend outside Docker, Playwright downloads Chromium on first use into
`~/.cache/ms-playwright`. On a bare Linux machine it may need system libraries:
```
cd backend && ./mvnw exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install-deps chromium"
```
In the backend image Chromium and its libraries are already installed.

## Common failures

**`port is already allocated` (5432 / 9000 / 9001 / 1025 / 8025)**
Another project holds the port. Find it:
```
docker ps --filter "publish=5432" --filter "publish=9000"
```
Then start on different ports (the variables exist for this):
```
DB_PORT=5433 S3_PORT=9002 S3_CONSOLE_PORT=9003 docker compose up -d
```
Pass the same `DB_PORT` / `S3_PORT` to the backend if it runs outside a container.

**`Schema-validation: missing column [...]` at startup**
An entity changed without its Flyway migration (Hibernate runs with `ddl-auto: validate`). Add
the migration → `db-migration` skill.

**`Flyway ... checksum mismatch`**
A migration that was already applied has been edited. Restore its original content and add a
new migration. In development only, you can start from scratch:
```
docker compose down -v && docker compose up -d
```
(`-v` drops the volumes, and therefore the local data — never in production.)

**A portfolio check fails on `localhost` or a private IP**
On purpose: the checker refuses private and loopback addresses (SSRF). Test checks against the
local HTTP server of the `*IT` suite, or a real public URL.

## Do not

- Do not run `docker-compose.prod.yml` locally: it expects the Traefik network, the Docker Hub
  images and a production `.env`.
- Do not recreate a `docker-compose.yml` under `backend/` or `frontend/`: one compose file per
  environment, at the root.
