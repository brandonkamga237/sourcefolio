# SourceFolio

A platform that turns the public GitHub contributions of tech profiles (developers, SWE,
sysadmins, DevOps, data, mobile, QA, security) into a profile recruiters can read and trust.
Promise: "De l'open source à l'opportunité" / "From open source to opportunity". The audience is
African; the map shows it rather than the copy repeating it. The name is provisional. Product
spec: `docs/spec-v2.md` (it replaces `docs/spec-v1.md`, the former portfolio catalogue, which is
being removed from the product).

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
| `frontend/` | `pnpm icons` | regenerates `components/icons/` from `icons/*.svg` (SVGR) |
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
- Nothing is public without the member's explicit "Publish"; unpublishing takes effect at once
- Every figure on a profile links to the GitHub page that proves it. The profile measures
  public open source contribution, not a person's total worth, and says so
- Rankings sort on one factual indicator at a time and state what they measure; no combined
  score, no streaks, no points. Hand-entered fields (bio, LinkedIn, city) never enter a ranking.
  A beginner must feel welcome: an empty calendar is a starting point, not a zero to display
- On-screen copy says "tech profile" / "profil tech" where the context is general, so that a
  sysadmin or a DevOps recognises themselves, not only "developer"
- Visual language: GitHub Primer palette (light, dark, system) and the "green square" concept:
  the contribution square is the unit of the interface (logo, Africa map mosaic, beginner path,
  talent field)
- One main action per screen; the rest is progressive disclosure
- Comments explain why, never what. No file headers, no generated javadoc, no commented-out code
- Icons and the logo come from our own pack (`frontend/icons/*.svg` → SVGR): never Lucide,
  Heroicons or any icon library
- The current controllers, DTOs and migrations are the truth for what exists; `docs/spec-v2.md`
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
- Commit far more often than usual: many small commits, each passing its area's checks
- PRs target `develop` and are merged with a merge commit (every commit kept) by
  `scripts/git/merge-to-develop.sh <pr>` once CI is green. Only the maintainer merges `develop` into `main` (merge commit); the release PR
  then merges itself once its checks pass, which tags and deploys
- Releases are cut by release-please from the commit types (`fix` patch, `feat` minor, `!`
  major). Never edit `CHANGELOG.md`, `version.txt` or `.release-please-manifest.json`
- The step-by-step lives in the `git-workflow` skill; the full policy in `CONTRIBUTING.md`

## Never

- `git push --force`, committing `.env`, hardcoding S3 keys, the GitHub OAuth secret or any token
- Editing a Flyway migration that is already merged: add the next `V{n}` instead
- Asking GitHub for more than the public profile (`read:user`): no `repo`, no `read:org`
- Calling the GitHub API with anything but the dedicated server token (secret, never in the
  repository); the sign-in token is not stored for API calls
- Collecting people who have not signed up, reading LinkedIn, or reading private repositories
- Translating member content automatically (bios) in V2
- Showing illustration data without a visible "Example" label: no fake testimonials, logos,
  figures or profiles
- Inventing data the backend does not provide

## Vocabulary

- **Tech profile** (profil tech): a member, whatever their job (developer, SWE, sysadmin,
  DevOps, data, mobile, QA, security). Signs in with GitHub
- **Analysis**: one reading of a member's public GitHub data, producing a snapshot of the six
  indicators with its date. Runs at sign-in (at most hourly) and weekly
- **Indicators**: seniority, volume (public contributions over 12 months), regularity (active
  months out of 12), stars and followers, languages, contributions accepted elsewhere (merged
  pull requests in repositories the member does not own). Definitions in `docs/spec-v2.md`
- **Profile** (fiche): the page built from the latest snapshot plus the hand-entered fields;
  private preview first, public only once published, at a stable address
- **Ranking** (Classement): published profiles sorted by one indicator, filtered by country and
  language
- **Map** (Carte): the mosaic of Africa where each square shows the density of published
  profiles, by city
- **Waitlist**: recruiters leave a work e-mail; no recruiter account in V2
- **Showcase, Check, Lifecycle, Feedback, Discover**: the former portfolio catalogue
  (`docs/spec-v1.md`), being removed from the product; its tables stay until a decision

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
