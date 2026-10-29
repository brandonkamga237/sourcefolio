# SourceFolio

![Java 21](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4-6DB33F?logo=springboot&logoColor=white)
![Next.js](https://img.shields.io/badge/Next.js-000000?logo=nextdotjs&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?logo=typescript&logoColor=white)
![PostgreSQL 18](https://img.shields.io/badge/PostgreSQL-18-4169E1?logo=postgresql&logoColor=white)

Portfolios you can read the code of: a catalogue of developer portfolios that are online and
whose source is public. Visitors browse by country, specialty and stack; authors publish in
one click after signing in with GitHub, and get feedback, private stats and a health report of
their site. French and English. The name is provisional.

| | |
| --- | --- |
| [`backend/`](backend/) | REST API and background jobs — Spring Boot 4, Java 21, PostgreSQL, JPA, Flyway, JobRunr, Playwright |
| [`frontend/`](frontend/) | Web app and PWA — Next.js, React, TypeScript, Tailwind, Radix, TanStack Query, next-intl |
| [`docs/spec-v1.md`](docs/spec-v1.md) | Product specification V1 |

## Run it locally

Requirements: Git, Docker, JDK 21, Node 24 and pnpm. No configuration file is needed.

1. Start the services the app depends on (PostgreSQL, S3 storage, a mail catcher):

   ```bash
   docker compose up -d
   ```

2. Start the API:

   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. In a second terminal, start the web app:

   ```bash
   cd frontend
   pnpm install
   pnpm dev
   ```

4. Open http://localhost:3000. Signing in needs a GitHub OAuth App: see
   `.claude/skills/dev-setup/SKILL.md`.

To stop the services: `docker compose down` (add `-v` to also erase the local data).

## Contributing

Branches, commit format and review rules are in [CONTRIBUTING.md](CONTRIBUTING.md).

---

© 2026 Brandon Kamga. All rights reserved.
