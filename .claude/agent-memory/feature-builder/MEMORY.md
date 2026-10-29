# feature-builder memory

Build traps met in this repository. One line per fact.

- The commit hook refuses any mention of "claude" except the exact strings `CLAUDE.md` and `.claude/`: use the `(tooling)` scope for `.claude/` changes
- `gh pr merge` and pushes to `main`/`develop` are denied; merging into develop is `scripts/git/merge-to-develop.sh <pr>`, only when the caller asks
- Spring Boot 4 splits auto-configuration per technology: Flyway, JPA and others need their own `spring-boot-starter-*`, not just the library
- Every new user-facing string goes into both `frontend/messages/fr.json` and `frontend/messages/en.json`
