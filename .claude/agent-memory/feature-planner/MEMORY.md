# feature-planner memory

Decisions the maintainer took and traps for later plans. One line per fact.

## Decisions
- Persistence is Spring Data JPA, kept inside `infra/persistence/` behind domain ports; the domain never sees an entity (2026-10)
- Production is one domain with path routing (`/api`, `/oauth2`, `/login/oauth2` → backend, the rest → Next.js): same origin, no CORS (2026-10)
- Screenshots: RustFS in development, Cloudflare R2 in production; MinIO images are no longer published on Docker Hub (2026-10)
- No Keycloak: members sign in with GitHub through Spring Security OAuth2 Client, scope `read:user` only (2026-10)
- `docs/spec-v1.md` holds the product decisions (delays, lifecycle, feed rules); cite it, do not restate it

## Traps
