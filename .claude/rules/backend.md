---
paths:
  - "backend/**"
---

# Backend conventions

The architecture, its patterns and the pre-commit checklist are in the `design-patterns` skill:
load it before any backend change. `ArchitectureTest` enforces the mechanical part in CI.

## Structure

- Each module follows `api/` + `api/dto/`, `application/`, `domain/model/` + `domain/port/`,
  `infra/persistence/`, `infra/<technology>/` (`github/`, `playwright/`, `s3/`, `smtp/`,
  `jobs/`…), `infra/config/`
- Dependency direction `api → application → domain ← infra`. `domain` and `application` are
  plain Java: no Spring, JPA, servlet, JobRunr, Playwright or Micrometer import
- Application services are built in `infra/config` and wrapped by `TransactionalServices`;
  methods named `find/require/list/search/count…` run read-only
- Another module is reached through its `application` interfaces or a domain event, never its
  `domain/port` or `infra`
- `shared/` holds paging, errors, clocks, locale, slugs and the transactional proxy once; never
  copy them into a module

## HTTP

- Controllers parse, call one use case, and return a response record; requests convert
  themselves into commands (`request.toXxx()`), responses build themselves (`XxxResponse.of(...)`)
- Never return a domain model or a JPA entity
- Errors are thrown as `NotFoundException`, `BadRequestException`, `ForbiddenException`,
  `ConflictException` or `UnauthenticatedException`; `GlobalExceptionHandler` sets the status
  and returns a translation key plus the message in the request locale
- A module opens its routes with a `SecurityCustomizer` bean in `<module>/infra/security/`;
  `shared/security/SecurityConfig` holds the general rules only. Members sign in with GitHub
  (Spring Security OAuth2 Client, scope `read:user` only) and keep a server session stored by
  Spring Session JDBC; moderator methods carry `@PreAuthorize("hasRole('MODERATOR')")`
- Every endpoint shows up in springdoc: the frontend types are generated from it

## Persistence

- Spring Data JPA, inside `infra/persistence/` only: `XxxEntity` (JPA) is mapped to the domain
  model by hand in `JpaXxxStore`, which implements the domain port. The domain never sees an
  entity, a `Page` or a repository
- `spring.jpa.open-in-view=false` and `ddl-auto=validate`: Flyway owns the schema, Hibernate
  only checks it
- No query in a loop and no lazy loading in a mapper: fetch joins, `@EntityGraph` or a DTO
  projection per list; counts and sums done by PostgreSQL
- The schema changes only through a new Flyway migration (`db-migration` skill); never edit one
  that is merged. Index every foreign key and every filtered or joined column
- Side effects (email, S3, HTTP to GitHub or to a portfolio) run outside the transaction,
  usually as a JobRunr job enqueued after commit (`background-job` skill)

## Tests

- Mirror the production package: `src/test/.../<module>/{api,application,domain,infra}/`
- Application tests use copying in-memory fakes (`InMemoryXxxStore`) and assert on what was
  stored; gateways (GitHub, Playwright, S3, mail) are faked or mocked
- `*Test` runs with `./mvnw test` (no Docker); `*IT` extends `support/PostgresIT` and runs with
  `./mvnw verify`. Every repository query of an adapter has an `*IT`
