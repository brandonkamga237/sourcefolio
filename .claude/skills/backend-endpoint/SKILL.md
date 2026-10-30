---
name: backend-endpoint
description: Add or change a REST endpoint or a use case in the SourceFolio backend, step by step — request/response, command, application service, port and adapter, assembly, controller, security, OpenAPI and the generated frontend types. Follows the design-patterns skill. Use whenever the Spring Boot API or its business code changes (new route, newly exposed field, new email, changed permissions).
---

# Adding an endpoint

Read `.claude/skills/design-patterns/SKILL.md` first: it defines the modules, the skeleton,
the dependency rule and the patterns. This file is the step-by-step for one endpoint.

## The full chain

Everything lives under `<module>/` (`account`, `profile`, `showcase`, `checkup`, `discovery`,
`taxonomy`, `engagement`, `notification`, `moderation`, `storage`).

1. **Request / response** — `<module>/api/dto/XxxRequest.java`, `XxxResponse.java`.
   Java records. Validation (`@NotBlank`, `@Size`, `@Pattern`…) on requests only, with
   messages that are translation keys (`{feedback.body.size}`). Never expose a domain model or
   an entity.
   - A request translates itself into a command: `public PostFeedback toCommand(UserId author)`.
   - A response builds itself from the read model: `public static ShowcaseCardResponse of(CatalogEntry e)`.
   - A like count is never in a public response; it appears only in the author's dashboard
     responses.

2. **Command** — `<module>/application/Xxx.java`, a record with only what the use case
   needs. No validation annotation, no JSON concern.

3. **Use case** — interface `<module>/application/XxxService.java` + `XxxServiceImpl.java`,
   plain Java. It loads aggregates through ports (`domain/port`), applies the rules held by
   the model (`domain/model`), and **saves explicitly** through the port. Time comes from the
   injected `Clock`.

4. **Port and adapter** — when the use case needs I/O that no port offers yet, add the
   interface in `domain/port` and its implementation in `infra/<technology>/` or
   `infra/persistence/JpaXxxStore` (entity, Spring Data repository, hand mapping, an `*IT`
   covering every query). A schema change goes through the `db-migration` skill. Slow I/O
   (GitHub, a portfolio, S3, mail) goes through a job: `background-job` skill.

5. **Assembly** — `<module>/infra/config/XxxServices.java` builds the service with
   `transactional.wrap(XxxService.class, new XxxServiceImpl(...))`. Name read methods
   `find/require/list/search/count…` so they run read-only.

6. **Controller** — `<module>/api/XxxController.java`: parse, call one service method,
   shape the response. Nothing else.
   ```java
   @PostMapping("/showcases/{id}/feedback")
   public ResponseEntity<FeedbackResponse> post(@PathVariable UUID id,
                                                @Valid @RequestBody FeedbackRequest request,
                                                @AuthenticationPrincipal Member member) {
       return ResponseEntity.status(201).body(FeedbackResponse.of(feedback.post(request.toCommand(id, member.id()))));
   }
   ```
   Errors: throw `NotFoundException`, `BadRequestException`, `ForbiddenException`,
   `ConflictException` or `UnauthenticatedException` (`shared/exception`); they carry an
   `ErrorCode` and `GlobalExceptionHandler` maps it to the status and a localised message.
   Never build a status by hand.

7. **Security** — `shared/security/SecurityConfig.java` holds the general rules: `/api/**`
   needs a session, everything unlisted is denied, writes need the CSRF token (`csrf.spa()`,
   cookie `XSRF-TOKEN` echoed in `X-XSRF-TOKEN`). A module opens its own routes with a
   `SecurityCustomizer` bean in `<module>/infra/security/`, method by method:
   ```java
   @Bean
   SecurityCustomizer catalogRoutes() {
       return http -> http.authorizeHttpRequests(routes -> routes
               .requestMatchers(HttpMethod.GET, "/api/catalog/**").permitAll());
   }
   ```
   Customizers run before the general rules, so their matchers win; `shared` never names a
   module. See `account/infra/security/GitHubLogin.java`. `/api/moderation/**` needs
   `hasRole("MODERATOR")`; add `@PreAuthorize` on those controllers too.

8. **OpenAPI** — springdoc picks the controller up. Give the operation a stable
   `operationId` (`@Operation(operationId = "postFeedback")`): the frontend's generated types
   and query keys use it.

## Conventions

- Members sign in with GitHub only (Spring Security OAuth2 Client, scope `read:user`). The
  `AuthIdentity` row links the provider account to a `User`; the rest of the code knows the
  `User` id, never the GitHub login. In a controller, the member id is
  `UUID.fromString(authentication.getName())` (the session principal's name); other modules ask
  `account`'s `SignInService.requireMember(id)` for the login and language.
- An action that needs a session from a visitor (like, comment) returns 401; the frontend
  starts the GitHub sign-in and replays the action on return.
- User-facing text (emails, error messages) exists in French and English, picked from the
  recipient's or the request's locale; everything else is English.
- Logging: `slf4j`, INFO for business actions, DEBUG for technical detail. Never log a token,
  an email address or the body of an email.
- Files go through `storage` (S3), never the local disk.
- Emails go through `notification` (`Mailer.send(new XxxEmail(...))`), never `JavaMailSender`
  directly.

## Tests that come with the change

Mirror the production package (`src/test/.../<module>/{api,application,domain,infra}/`).
- Every new use case: a `*Test` with fakes for stores (`InMemoryXxxStore`, copying), a fixed
  clock and fakes for gateways, one test per rule (`@DisplayName` states the rule in a
  sentence). Assert on what was stored.
- Every new email template: assert the link, the escaping, both languages and the text part.
- Persistence or query changes: a `*IT` extending `support/PostgresIT`.
- New route: a `@WebMvcTest` for status codes, validation and who may call it.

## On the frontend side

Regenerate the types, then call the endpoint from `frontend/services/` only:
```
cd frontend && pnpm api:types     # backend running on :8080
```
Add the typed function and its TanStack Query key to the matching `services/<area>.ts`.

## Verify
```
cd backend && ./mvnw test      # ./mvnw verify when persistence changed (needs Docker)
```
Then check the route in Swagger: `http://localhost:8080/swagger-ui.html`.
