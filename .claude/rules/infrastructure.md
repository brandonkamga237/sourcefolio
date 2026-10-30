---
paths:
  - "docker-compose*.yml"
  - ".env.example"
  - ".github/**"
  - "**/Dockerfile"
  - "monitoring/**"
  - "docker/**"
---

# Infrastructure conventions

- One compose file per environment, at the root: never create one under `backend/` or
  `frontend/`
- Never rename `docker-compose.prod.yml`: `deploy.yml` copies and runs it by name. Its backend
  and frontend images take their tag from `IMAGE_TAG`
- Production routing is by path on one domain (Traefik): `/api`, `/oauth2`, `/login/oauth2`
  go to the backend, everything else to Next.js. Same origin, so no CORS and a plain
  `SameSite=Lax` session cookie
- A new environment variable goes into `.env.example` and `docker-compose.prod.yml` in the same
  commit. A secret never gets a default value in production configuration
- Pin image versions; `latest` makes a restart change what runs. Before pinning, check that the
  tag exists on the registry
- Screenshots are stored in an S3-compatible bucket: RustFS in development, Cloudflare R2 in
  production. The backend only knows the `S3_*` variables, never the provider
- The frontend image is built per public origin: `SITE_URL` is a build argument (GitHub
  variable `SITE_URL` in `deploy.yml`) because Next.js writes it into the prerendered pages
- The backend image carries Chromium for Playwright; keep its version in step with the
  `com.microsoft.playwright` dependency
- CI (`.github/workflows/ci.yml`) gates every PR; `release.yml` (release-please) tags versions;
  `deploy.yml` ships a tag to the VPS, and a rollback is a manual run of it with an earlier tag
- Production is checked read-only with the `release-verifier` subagent; server details stay out
  of the repository (`.claude/release-verifier.local.md`, not committed)
