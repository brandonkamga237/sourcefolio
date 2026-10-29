---
name: background-job
description: Add or change background work in the SourceFolio backend with JobRunr — showcase checks, Playwright screenshots, GitHub sync, lifecycle timers, digests and emails — keeping use cases plain and idempotent. Use whenever something must run later, on a schedule, outside a request, or call a slow or unreliable outside system.
---

# Background jobs (JobRunr)

## When a job, when not

A job when the work calls the outside (a portfolio, GitHub, S3, SMTP), takes more than a few
hundred milliseconds, must retry, or runs on a schedule. Not a job: anything the HTTP response
needs to show right away.

The three job families of the spec, each on its own JobRunr queue and worker count:

| Family | Queue | What | Trigger |
| --- | --- | --- | --- |
| Check | `checks` | HTTP probe, fingerprint, health report, lifecycle decision | every 24 h per published showcase; +1 h and +6 h after a failure; every 6 h while `UNAVAILABLE`; right after publication |
| Screenshot | `screenshots` | Playwright mobile (390×844) and desktop (1440×900) captures, WebP/AVIF variants to S3 | after a check whose fingerprint changed, or on publication |
| GitHub sync | `github` | scan the member's public repos for candidates, refresh `detected_stack`, homepage and bio | right after sign-in, then daily |

Plus recurring housekeeping: lifecycle timers (14 → `NEEDS_FIX`, 21 → `REMOVED`, removal
notice 30 days before the 12-month deletion), the weekly author stats notification, the
optional weekly digest.

## The shape

```
<module>/application/CheckupService.check(ShowcaseId)      plain Java, idempotent
<module>/infra/jobs/CheckShowcaseJob.java                  @Job adapter: ids in, one call out
<module>/infra/jobs/CheckupScheduler.java                  implements domain/port/CheckScheduler
<module>/infra/config/CheckupJobs.java                     recurring jobs, queue settings
```

- The use case never imports JobRunr. When it must schedule something (retry at +1 h), it
  calls a port: `CheckScheduler.scheduleRetry(showcaseId, at)`; the JobRunr adapter implements it.
- Job arguments are ids and small values, never entities or domain objects: a job is stored as
  JSON and may run after a deployment changed the classes.
- Idempotent by design: running the same job twice gives the same state (a check for a showcase
  already `REMOVED` exits; a screenshot overwrites the same S3 key).
- A job is enqueued **after commit** (from an after-commit event listener), so it never sees a
  row that was rolled back and never runs before the row is visible.
- One job, one aggregate: a recurring "check everything due" job selects due ids with one
  query and enqueues one job per showcase; it never loops over network calls itself.
- Retries: JobRunr's automatic retries are for infrastructure failures (S3 down). A portfolio
  that does not answer is **not** a job failure: it is a check result (`UNREACHABLE`) the use
  case records. Mark it `@Job(retries = 0)` and let the lifecycle decide.

## Outside calls

- HTTP to portfolios: short timeouts (connect 5 s, total 15 s), follow at most 5 redirects,
  identify ourselves (`SourceFolioBot/1.0 (+https://<domain>/bot)`), never send cookies,
  refuse private and loopback addresses after DNS resolution (SSRF).
- Playwright: one shared `Browser` per worker, a fresh `BrowserContext` per capture, JavaScript
  on, a 20 s navigation budget, the context closed in `finally`. Chromium version follows the
  `com.microsoft.playwright` dependency and the backend image.
- GitHub: unauthenticated or app-token REST calls on public data only; respect
  `X-RateLimit-Remaining` and back off; store `ETag` to make refreshes cheap.

## Tests

- The use case: `*Test` with a fixed clock and fakes (`FakePageRenderer`, `FakeSiteProbe`,
  `RecordingCheckScheduler`); one test per lifecycle rule ("third failure in 24 h hides the
  showcase", "a site answering again republishes it with its feedback").
- The adapter: a thin `*IT` that runs the job against PostgreSQL and a local HTTP server
  (WireMock or a `HttpServer`), never a real portfolio on the internet.
- Never `Thread.sleep` to wait for a job; call the use case directly.

## Verify

```
cd backend && ./mvnw test
```
In development, the JobRunr dashboard runs on `http://localhost:8000` (dev profile only, never
routed in production).
