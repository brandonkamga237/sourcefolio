# SourceFolio

A catalogue of developer portfolios that are online and whose code is public: you can see the
site and read how it is built. Promise: "Des portfolios dont tu peux lire le code" / "Portfolios
you can read the code of". The name is provisional. Product spec: `docs/spec-v1.md`.

Monorepo: `backend/` (Spring Boot 4, Java 21, PostgreSQL, JPA, Flyway, JobRunr, Playwright) and
`frontend/` (Next.js App Router, React, TypeScript, Tailwind, Radix, TanStack Query, next-intl,
Serwist PWA).

## Layout

- `backend/src/main/java/com/brandonkamga/sourcefolio/<module>/`: one package per business
  capability (`account`, `profile`, `showcase`, `checkup`, `discovery`, `taxonomy`,
  `engagement`, `notification`, `moderation`, `storage`). `shared/` holds cross-cutting code only
- `backend/src/main/resources/db/migration/`: Flyway migrations `V{n}__description.sql`
- `frontend/` (no `src/`, `@/` points at its root): `app/[locale]/` (routes), `components/` (`ui/` Radix primitives, `icons/`
  generated from `icons/*.svg`), `services/` (every API call), `hooks/`, `lib/`, `i18n/`;
  `frontend/messages/{fr,en}.json` hold every string on screen
- `docker-compose.yml` (dev), `docker-compose.prod.yml` (prod, deployed by CI), `.env.example`
- `monitoring/`: Prometheus, Grafana, Loki, Alloy, blackbox probes

## Commands

| Where | Command | What |
| --- | --- | --- |
| root | `docker compose up -d` | PostgreSQL, RustFS (S3) and Mailpit for development, no `.env` needed |
| `backend/` | `./mvnw spring-boot:run` | API on :8080, profile `dev` |
| `backend/` | `./mvnw test` | unit tests and `@WebMvcTest` slices, no Docker |
| `backend/` | `./mvnw verify` | also the `*IT` suite on PostgreSQL via Testcontainers (Docker) |
| `frontend/` | `pnpm install`, `pnpm dev` | Next.js on :3000, rewrites `/api` and `/oauth2` to :8080 |
| `frontend/` | `pnpm typecheck`, `pnpm lint`, `pnpm build` | the checks CI runs |
| `frontend/` | `pnpm api:types` | regenerates `lib/api/schema.d.ts` from the running API's OpenAPI |
| `frontend/` | `pnpm test:e2e` | Playwright end-to-end tests |

Use `pnpm`, never `npm`. Before a commit, the checks of the area you touched must pass:
`./mvnw test` for the backend, `pnpm typecheck && pnpm lint` for the frontend.

The frontend scripts above are the contract: whoever scaffolds `frontend/` defines them with
these names, because CI, the skills and the agents call them.

## Rules that apply everywhere

- Code, identifiers, file names, branches, commits and comments are in English. User-facing
  text is bilingual, French and English, both complete: no string is written in code, a missing
  translation key blocks the release
- Mobile first: about 90 % of first visits come from a phone, often through a WhatsApp link.
  Touch targets are 44 px minimum, contrast AA, light and dark themes
- The product builds habits through usefulness (stats, feedback, health alerts), never through
  streaks, points or rankings. The number of likes is visible to its author only
- One main action per screen; the rest is progressive disclosure
- Comments explain why, never what. No file headers, no generated javadoc, no commented-out code
- Icons and the logo come from our own pack (`frontend/icons/*.svg` → SVGR): never Lucide,
  Heroicons or any icon library
- The current controllers, DTOs and migrations are the truth for what exists; `docs/spec-v1.md`
  is the truth for what is intended. When a document disagrees with the code, say so
- Backend, frontend and infrastructure conventions load from `.claude/rules/` when you work in
  those files

## Git workflow

- Never edit on `main` or `develop`: branch first, off `develop`, without tracking it:
  `git switch -c feature/x --no-track origin/develop` (`fix/`, `docs/`, `chore/` likewise;
  `hotfix/` branches off `main`)
- Commit as you go, one logical change per commit, header `type(scope): description` (≤ 100
  characters; types `feat fix perf refactor docs test build ci chore style revert`), checked by
  `scripts/git/check-commit-msg.sh`
- Never mention Claude, AI or a generation tool in a commit, PR title or PR body
- PRs target `develop` and are squash-merged with `scripts/git/merge-to-develop.sh <pr>` once
  CI is green. Only the maintainer merges `develop` into `main` (merge commit); the release PR
  then merges itself once its checks pass, which tags and deploys
- Releases are cut by release-please from the commit types (`fix` patch, `feat` minor, `!`
  major). Never edit `CHANGELOG.md`, `version.txt` or `.release-please-manifest.json`
- The step-by-step lives in the `git-workflow` skill; the full policy in `CONTRIBUTING.md`

## Never

- `git push --force`, committing `.env`, hardcoding S3 keys, the GitHub OAuth secret or any token
- Editing a Flyway migration that is already merged: add the next `V{n}` instead
- Asking GitHub for more than the public profile (`read:user`): no `repo`, no `read:org`
- Showing a public score, a like count or a ranking; a popularity sort ("Populaires")
- Translating member content automatically (bios, comments) in V1
- Embedding a portfolio in an iframe: external sites open in a new tab, and the click is counted
- Inventing data the backend does not provide

## Vocabulary

- **Showcase**: what a member publishes; in V1 always `kind = portfolio`. One published
  showcase per member (business rule, not a schema limit)
- **Source repo**: the public repository a showcase is built from; same repo means same
  showcase when the URL changes
- **Check**: one verification of a showcase (HTTP status, response time, content fingerprint,
  health report, mobile and desktop screenshots)
- **Lifecycle**: `PUBLISHED` → `UNAVAILABLE` (3 failures in 24 h, hidden from the catalogue) →
  `NEEDS_FIX` (14 days) → `REMOVED` (21 days); back to `PUBLISHED` as soon as the site answers.
  A radical content change or a parked domain goes straight to `NEEDS_FIX`
- **Health report** (bilan de santé): private to the author, never a public score
- **Feedback** (retour): a comment with an optional angle (`DESIGN`, `CLARITY`, `PERFORMANCE`,
  `MOBILE`, `CODE`) and 10 to 1 000 characters
- **Discover**: the feed, shuffled once a day, one card in five reserved for showcases younger
  than 14 days. **Recent** is the only other sort

## Where to look

- Skills (`.claude/skills/`): `design-patterns` (backend architecture, mandatory for backend
  changes), `backend-endpoint`, `db-migration`, `background-job`, `frontend-page`,
  `design-system`, `seo-page`, `dev-setup`, `git-workflow`, plus UI and motion skills
  (`emil-design-eng`, `animate`, `mobile-native`, `apple-design`…)
- Subagents (`.claude/agents/`), each with an evidence-first report format:
  - `feature-planner` plans a feature from the code and the spec (read-only) →
    `feature-builder` builds it on a branch, committing as it goes
  - `bug-fixer` reproduces a bug with a failing test, fixes the cause, keeps the test
  - `schema-keeper` writes migrations and JPA mapping changes, and the production checks they need
  - `dependency-upgrader` adopts a dependency bump from its official release notes
  - `architecture-reviewer` reviews a backend branch before a PR (read-only)
  - `release-verifier` checks production after a deployment (read-only, hook-guarded)
- Agent memory lives in `.claude/agent-memory/<agent>/` and is committed: review what agents
  write there like code. Agents that read external content (web pages, server output) keep none
