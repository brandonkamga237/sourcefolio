---
name: design-patterns
description: The architecture, SOLID rules and design patterns every SourceFolio backend change must follow — module map, ports and adapters, JPA kept inside persistence adapters, no N+1 and performance rules, background work through JobRunr, where each pattern applies, what to reuse from shared, and the checklist to run before committing. Use for every backend feature, fix or refactor, and whenever deciding where a class goes or whether a pattern is worth it.
---

# Architecture and design patterns

Non-negotiable: every module, feature and fix follows this file. A change that does not fit
it is redesigned, not merged. `ArchitectureTest` enforces the mechanical part in CI; the rest
is on the author and the reviewer.

A modular monolith on PostgreSQL, one deployable, three kinds of background work (checks,
screenshots, GitHub sync) run by JobRunr inside the same process. No microservice in V1.

## 1. Modules: one business capability each

| Module | Owns | Never owns |
| --- | --- | --- |
| `account` | `User`, `AuthIdentity` (provider `github`, provider user id, login), GitHub sign-in, session, locale, optional email, account deletion | Profile fields, showcases |
| `profile` | `DeveloperProfile`: display name, bio + bio language, country (ISO 3166), specialty, technologies, the origin of each field (`GITHUB` or `DECLARED`), the one-question-at-a-time completion | Credentials, showcases |
| `showcase` | `Showcase`, `SourceRepo`, the lifecycle and its `ShowcaseStatusEvent` history, candidate detection from GitHub repos, automatic vs manual publication, the one-published-per-member rule | Checks, screenshots, reactions |
| `checkup` | `ShowcaseCheck`: HTTP status, response time, content fingerprint, parked-domain detection, health report (links, OG image, title/description, mobile rendering), mobile and desktop screenshots | The lifecycle decision itself (it reports, `showcase` decides) |
| `discovery` | The catalogue read model: the daily shuffle with one card in five under 14 days, Recent, search, filters and their live count | Writes to showcases or profiles |
| `taxonomy` | `Specialty`, `Technology` (slug, aliases, FR/EN labels), country labels | — |
| `engagement` | `Reaction` (like), `Comment` (feedback with angle), `ShowcaseView` events (`VIEW`, `SITE_CLICK`, `CODE_CLICK`, country, no personal identifier) and the author stats built on them | The showcase itself |
| `notification` | `Notification`, in-app list, FR/EN email templates, weekly digest | Deciding who should be told (the emitting module decides) |
| `moderation` | `ModerationReport`, decisions, the manual verification queue (organisation repos, unclear ownership) | — |
| `storage` | Objects in the S3 bucket (screenshots and their WebP/AVIF variants), public URLs | What the files mean |
| `community` (V1.x) | Schools, countries, groups and their showcases | — |
| `shared` | Reusable building blocks (below) | Business rules of any module |

A new feature first answers: *which capability is this?* If the answer is "two of them", it is
two changes talking through an interface or an event. If no module fits, propose a new one —
never stretch an existing module to hold an unrelated concept.

How the lifecycle splits, as the reference example:

```
JobRunr ─▶ checkup: CheckShowcase(id) ─▶ stores ShowcaseCheck ─▶ publishes CheckCompleted
                                                                     │
showcase: ShowcaseLifecycle.onCheck(...) ◀───────────────────────────┘
          applies 3 failures / 14 days / 21 days, writes ShowcaseStatusEvent,
          publishes ShowcaseStatusChanged ─▶ discovery hides or shows the card
                                           ─▶ notification tells the author
```

## 2. The skeleton (same in every module)

```
<module>/
  api/                  controllers: parse, call one use case, shape the response
  api/dto/              XxxRequest (validation, toCommand()), XxxResponse (static of(...))
  application/          use cases: XxxService interface + XxxServiceImpl, command records
  domain/model/         aggregates, value objects, enums, domain events — plain Java
  domain/port/          interfaces the module needs from outside: stores, gateways, clocks
  infra/persistence/    XxxEntity (JPA), XxxJpaRepository (Spring Data), JpaXxxStore (the adapter)
  infra/<technology>/   github/, playwright/, s3/, smtp/, jobs/… one adapter per port
  infra/config/         @Configuration assembling application services, with their transactions
```

Dependency rule, checked by `ArchitectureTest`:

```
api ──▶ application ──▶ domain ◀── infra
```

- `domain` and `application` are plain Java: no `org.springframework`, `jakarta.persistence`,
  `jakarta.servlet`, `org.hibernate`, `org.jobrunr`, `com.microsoft.playwright`,
  `software.amazon`, `io.micrometer`. Lombok, slf4j and pure libraries (no I/O, no framework
  state) are allowed.
- `domain` imports nothing from `application`, `api` or `infra`.
- `infra` implements ports; it never holds a business rule.
- Another module is reached through its `application` interfaces (or a published event),
  never its `domain/port`, `infra` or entities. The types those interfaces expose — read
  models, commands, events, enums such as `ShowcaseStatus` — are its published language.
- Spring annotations live in `api` and `infra` only. Application services are built in
  `infra/config` and wrapped by `TransactionalServices` (reads by name prefix
  `find/require/list/search/count` are read-only, everything else read-write).

## 3. SOLID, concretely

- **S** — one service per use-case family (`ShowcaseDetection`, `ShowcasePublication`,
  `ShowcaseLifecycle`), not one per table. Over ~150 lines or more than five collaborators:
  it does two jobs, split it.
- **O** — a new variant is a new class: a new email is a record + an `EmailTemplate`; a new
  health criterion is a new `HealthProbe`; a new login provider is a new `AuthProvider` value
  and adapter, not a new table.
- **L** — every implementation of a port honours its contract, test doubles included
  (`InMemoryShowcaseStore` copies like a database, so a forgotten `save` fails the test).
- **I** — narrow ports: other modules get `ProfileDirectory`, not the whole profile service.
- **D** — services depend on ports, receive them by constructor, never `new` a collaborator,
  never field injection.

## 4. The patterns we use — and when

| Pattern | Use it for | Example |
| --- | --- | --- |
| **Ports & Adapters** | Anything that touches I/O: database, GitHub, browser, S3, mail, metrics | `RepositoryHost` ← `GitHubRepositoryHost`; `PageRenderer` ← `PlaywrightPageRenderer` |
| **Repository (as a port)** | Loading and saving an aggregate | `ShowcaseStore.save(showcase)` returns the stored copy; always save explicitly |
| **Data Mapper** | JPA entity ⇄ domain model, inside `infra/persistence` only | `JpaShowcaseStore.toDomain(entity)` / `toEntity(model)` |
| **State machine** | The showcase lifecycle | `Showcase.recordCheck(result, now)` returns the transition; illegal moves throw |
| **Aggregate** | A cluster changed together, one root, one store | `Showcase` with its `SourceRepo`; reference other aggregates **by id** |
| **Value Object** | A concept with rules and no identity | `CountryCode`, `SiteUrl`, `RepoCoordinates`, `ContentFingerprint`, `FeedbackBody` (10–1000 chars), `PageQuery` |
| **Command** | A use case with several inputs | `PublishShowcase`, `ProfileAnswer`; built by `Request.toCommand()` |
| **Strategy + Registry** | Several variants of one behaviour picked at runtime | `HealthProbe`s run by the check; `EmailTemplate<T>` in `EmailTemplates`; candidate rules (`*.github.io`, `homepage`, repo named `portfolio`/`site`/`me`) |
| **Facade** | What another module may know about yours | `ProfileDirectory`, `ShowcaseCatalog` |
| **Domain Event (Observer)** | A side effect owned by another module | `ShowcaseStatusChanged` → discovery, notification; published through a port, delivered by Spring after commit |
| **Decorator / Proxy** | Cross-cutting behaviour around a service | `TransactionalServices.wrap` adds the transaction without touching the service |
| **Query Object** | Combinable filters | `CatalogSearch(country, specialty, technologies, sort, cursor)` turned into a JPA Specification or a native query with bound parameters in the adapter |
| **Anti-corruption layer** | Translating an outside model | `GitHubRepositoryHost` turns GitHub JSON into `RepoCandidate`; the domain never sees GitHub's field names |
| **Unit of Work** | One business action = one transaction | The transactional proxy; jobs and emails enqueued after commit |

Do **not** add a pattern for a single case: an interface with one implementation that will
never vary (ports are the exception: they exist for the dependency rule and for tests), a
factory for one constructor, a generic `BaseService<T>`, an abstract class "for later". Two
real variants first, then the abstraction.

## 5. Reuse before writing: shared

Before writing any of these, use or extend the one in `shared`:

- Paging: `PageQuery`, `Paged<T>`, `Cursor` (domain) and `PageResponse.of(paged, mapper)` (api).
- Errors: `NotFoundException`, `BadRequestException`, `ConflictException`, `ForbiddenException`,
  `UnauthenticatedException` — they carry an `ErrorCode` (a translation key); never an HTTP status.
- Time: a `Clock` port, injected; never `Instant.now()` in `domain` or `application`, the
  lifecycle delays must be testable.
- Locale: `SupportedLocale` (`FR`, `EN`) and the request locale resolver.
- `TransactionalServices`, the after-commit event publisher, slugs, the outbound HTTP client
  settings (timeouts, user agent): one implementation each.

If two modules need the same thing and `shared` lacks it, move it to `shared` in its own
commit first, then use it.

## 6. Persistence: JPA behind the port

Spring Data JPA is the persistence tool, and it stays in `infra/persistence/`.

- `XxxEntity` is a JPA class with no behaviour; `XxxJpaRepository extends JpaRepository`;
  `JpaXxxStore implements XxxStore` maps entity ⇄ domain by hand, next to the queries.
- The domain model is never annotated with `@Entity`; an entity never leaves `infra`.
- `ddl-auto: validate`, `open-in-view: false`. Flyway owns the schema.
- Associations are `LAZY`; a list that needs them loads them explicitly (fetch join,
  `@EntityGraph`, or a projection record). No `FetchType.EAGER`.
- Counters and toggles in one atomic statement (`@Modifying` JPQL or native
  `INSERT … ON CONFLICT DO NOTHING`), never read-modify-write in Java. A like is a unique
  `(user_id, showcase_id)` row, not a counter column.
- Many rows: `saveAll` with `hibernate.jdbc.batch_size`, or one `INSERT … SELECT`; never a loop
  of `save`.
- Native SQL is welcome where JPQL is awkward (the daily shuffle, full-text and trigram search,
  stats aggregates): bound named parameters, every column named, never `SELECT *`.

## 7. Performance: no N+1, ever

The rule: **the number of queries of a use case does not grow with the number of rows.**

- **No query inside a loop**, in a stream, or in a mapper — including the hidden one, a lazy
  association touched while mapping. Turn on `hibernate.generate_statistics` in a test when in
  doubt, and assert the statement count of list use cases.
- Lists read what they show: a projection record with only those columns, not the aggregate.
- Counts, sums and groupings are done by PostgreSQL (`count(*) FILTER (WHERE …)`, `GROUP BY`).
- The catalogue grows without bound: keyset pagination (`WHERE (sort_key, id) > (:k, :id)`),
  never a deep `OFFSET`. The daily shuffle is a stable key (`md5(id || :day)`), so a cursor
  survives scrolling.
- Every foreign key and every column used in a `WHERE`, `JOIN` or `ORDER BY` of a frequent
  query has an index, created in the same migration as the query that needs it. A new query
  on a large table is checked with `EXPLAIN ANALYZE` before merging.
- Transactions stay short: no network call (GitHub, a portfolio, S3, SMTP) inside one. Those
  run in a JobRunr job enqueued after commit.
- No cache without a measurement that asks for it; when there is one, the port decides what is
  cached, not the caller.

## 8. Database rules

- One table belongs to one module. Cross-module links are `xxx_id` columns with a foreign key;
  a module never queries another module's tables. `discovery` keeps its own read model
  (`catalog_entries`), fed by events, rather than joining showcases and profiles.
- Same concept, same shape everywhere: `TIMESTAMPTZ` for every instant, `VARCHAR(1000)` URLs,
  `status VARCHAR(20)` with a `CHECK`, ISO codes (`country_code CHAR(2)`, `lang VARCHAR(5)`),
  reference data stored as slugs (`frontend`, `react`) and translated at display time.
- Identify people by id, never by email or GitHub login (logins change).
- Generic names from day one: `showcase.kind`, `auth_identity.provider`, `reaction.type`, so
  projects, GitLab or new reactions need no new table.
- No personal identifier in `showcase_views`: country and event type only.
- Schema changes only through a new Flyway migration (`db-migration` skill). Data moves keep
  ids stable so foreign keys survive.

## 9. Background work

Checks, screenshots, GitHub sync, digests and the lifecycle timers are JobRunr jobs; see the
`background-job` skill. A job is an `infra/jobs/` adapter that calls one application use case
with ids, never entities. Use cases stay idempotent: a job may run twice.

## 10. Tests that prove the design

- `application`: unit tests with **fakes** for stores (`InMemoryXxxStore`, copying), a fixed
  `Clock`, and fakes for gateways (`FakeRepositoryHost`, `FakePageRenderer`); assert on what
  was stored, not on the object you passed in.
- `domain/model`: plain unit tests of the rules, the lifecycle state machine above all.
- `infra/persistence`: `*IT` on PostgreSQL (`PostgresIT`) for every query of the adapter:
  mapping both ways, filters, paging, conflicts, statement count of lists.
- `api`: `@WebMvcTest` slices for status codes, validation, locale of error messages and
  security.
- `ArchitectureTest`: add the module to its lists once it follows the skeleton.

## 11. Checklist before every commit

1. Which module owns this? Is it in the right folder of the skeleton?
2. Does `domain`/`application` import anything from Spring, JPA, JobRunr, Playwright, AWS,
   servlet, `infra` or `api`?
3. Is every change to an aggregate saved through its store? Does any entity leak out of `infra`?
4. Is anything here already in `shared` or another module? Reuse it.
5. Did I add an abstraction with a single case? Remove it.
6. Did I touch another module's tables, entities or repositories? Go through its interface.
7. No query in a loop, no lazy association touched in a mapper; lists use projections;
   aggregates computed by PostgreSQL; every filtered or joined column indexed.
8. Any network call inside a transaction? Move it to a job enqueued after commit.
9. Tests: application with fakes and a fixed clock, IT for every query, `ArchitectureTest` green.
