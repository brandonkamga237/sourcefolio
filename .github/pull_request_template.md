<!--
The title becomes the commit on the target branch (squash merge):
  type(scope): description   e.g. feat(engagement): add an angle to feedback
See CONTRIBUTING.md.
-->

## What

<!-- The change, from the user's or the developer's point of view. -->

## Why

<!-- The problem it solves, or the decision it records. -->

## Checks

- [ ] `cd frontend && pnpm typecheck && pnpm lint && pnpm build`
- [ ] `cd backend && ./mvnw test` (and `./mvnw verify` when persistence or jobs changed)
- [ ] New environment variable added to `.env.example` and `docker-compose.prod.yml`, or none
- [ ] New Flyway migration instead of editing an applied one, or no schema change
- [ ] New strings in both `messages/fr.json` and `messages/en.json`, or none
- [ ] Checked on a phone-sized viewport (touch targets, bottom bar, scroll restore), or no UI change

## Notes for the release

<!-- Anything to do on deploy: a migration, a variable to set, a manual step. Delete if none. -->
