# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Test
./mvnw test

# Run locally (requires MySQL on localhost:3306 or DB_HOST set)
./mvnw spring-boot:run

# Docker
docker compose up -d
docker compose logs -f app
docker compose down
```

## Architecture

Spring Boot 3.2.3 / Java 21 REST API. JPA on top of MySQL 8. Stateless JWT auth via Spring Security. External integrations to Spotify and TheCocktailDB via Spring Cloud OpenFeign clients with Spring Cache.

### Domain model

- **`User`** — auth principal (email, username, bcrypt password)
- **`Event`** — the party plan (name, date, time, embedded `Location`, `isPrivate`, creator FK to `User`); many-to-many to `Artist`, `Drink`, `Ingredient`; element-collection of food strings
- **`Artist`** — Spotify artist (id, name, images, genres) cached locally so events can reference them
- **`Drink`** — cocktail recipe sourced from TheCocktailDB (name, recipe text, externalId, isAlcoholic, thumbnail)
- **`DrinkIngredient`** — join with amount text (e.g. `"1 oz"`)
- **`Ingredient`** — drink ingredient (name only)
- **`Location`** — `@Embeddable` value type on `Event` (latitude, longitude, description)

### Request flow

```
RestController (e.g. /events)
  → @Valid PostEventRequest
  → Service (e.g. EventService.saveEvent)
    → SecurityContextHolder.getContext() to resolve current user
    → Repositories (JPA)
    → utils.ConvertUtils to map entity → response DTO
  → ResponseEntity<EventResponse>
```

### Auth

Stateless JWT. `JWTFilter` (extends `OncePerRequestFilter`) reads `Authorization: Bearer <token>`, validates via `JWTUtil`, sets `SecurityContextHolder` authentication. Public endpoints listed in `SecurityConfig`. Token lifetime is currently unbounded (no `expiresAt` claim).

### External integrations

- **Spotify** — OpenFeign clients in `service/spotify/` with `SpotifyAuthCaller` (token refresh) + `SpotifyCaller` (search artists, get genres). Configured in `config/SpotifyAuthFeignConfig.java` and `config/SpotifyTokenFeignConfig.java`.
- **TheCocktailDB** — OpenFeign client in `service/cocktail/`. Used only by `CocktailDbSeedService`, which is invoked from `tasks/ScheduledTasks` (gated by `application.seeding.should-seed`).

### Caching

Spring Cache. `config/CacheConfig.java` defines named caches (`drinks`, `genres`). `tasks/ScheduledTasks#evictAllCaches` clears all caches every 50 minutes.

### Background jobs

`tasks/ScheduledTasks`:
- `retrieveDrinks()` — every 100 minutes; if `should-seed=true`, hits TheCocktailDB and populates `Ingredient` + `Drink` tables (idempotent — checks `existsByName` before inserting)
- `evictAllCaches()` — every 50 minutes

## Key conventions

- **Controllers** are thin — `@Valid` request, delegate to service, return `ResponseEntity<>`.
- **Services** use `@AllArgsConstructor` / `@RequiredArgsConstructor` (Lombok) for constructor injection.
- **Entities** use `@Builder` + `@Getter` + `@Setter`; collection fields default to empty via `@Builder.Default`.
- **Exception handling** centralized in `exception/Handler.java` (`@ControllerAdvice`).
- **DTO mapping** in `utils/ConvertUtils.java` — static methods, no MapStruct.
- **Ingredient names are case-sensitive** in the seeded data; `IngredientRepository.getByName()` is exact-match.

## Gotchas

- **Schema is owned by Flyway** (Phase 1). `application.yml` runs `ddl-auto: validate`. Migrations live in `src/main/resources/db/migration/`. Never edit V1; add V2, V3, … instead. The `*_seq` tables created by Hibernate's TABLE generators MUST be seeded with `next_val=1` (V2 does this) — Hibernate does not auto-seed when Flyway owns the schema.
- **`JWT_SECRET` is required** (no default) and must be ≥ 32 chars. `JWTUtil.@PostConstruct` enforces this on boot.
- **CORS** is env-driven via `CORS_ALLOWED_ORIGINS` (comma-separated). `SecurityConfig` parses it; default covers Vite dev (5173) and the dakis-server FE container (8094).
- **Endpoint protection policy** is documented inline in `SecurityConfig#filterChain` (Phase 1 T8). `/drinks/**`, `/ingredients/**`, `/music/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health` are public; everything else requires auth. Rate-limit on the public endpoints is deferred to Phase 9.
- **Don't remove `/actuator/health` permitAll** — the docker-compose healthcheck will 401 and the container will be unhealthy forever.
- **Spotify `client-id`** is now env-driven via `SPOTIFY_CLIENT_ID` (default keeps the historical public id).
- **`EventService`** read methods are `@Transactional(readOnly = true)`; `saveEvent` is `@Transactional`. Without these, lazy collections on `Event` (notably `foodItems`) blow up during DTO mapping.
- **`ConvertUtils.mapEventToResponse`** wraps `event.getFoodItems()` in `new ArrayList<>(...)` to materialize the lazy proxy inside the session. Don't undo that.
- **Integration tests** extend `BaseIntegrationTest` (Testcontainers MySQL). The class itself is `@Transactional` so test-side repository reads can lazy-load. See `EventControllerIntegrationTest` as the template.
- **`MusicController` has commented-out `/recommendations`** endpoint — kept for reference; properly wired in Phase 8 (Spotify playlist generation).
- **`PostEventRequest.drinks: List<Integer>`** carries cocktail IDs as of Phase 7 (FE wizard's Suggested-cocktails panel populates it). Independent of `ingredients: List<Integer>`, which carries alcoholic-ingredient picks. Both M2M relations on `Event` are populated independently — no coherence rule between them.
- **`EventResponse` includes `creatorUsername`** — populated from `event.getCreator().getUsername()` in `ConvertUtils.mapEventToResponse`. Required by the FE to gate Edit/Delete button visibility. Null-safe: if `creator` is null (should not happen in production), the field is `null`.
- **`PUT /events/{id}` is a full replace** — all association lists (artists, drinks, ingredients, food) are replaced wholesale, same as create. There is no `PATCH` partial-update endpoint.
- **`DELETE /events/{id}` uses `eventRepository.delete(entity)`** — not `deleteById`. Must pass the managed entity so JPA cascade rules fire and clear `event_artists`, `event_drinks`, `event_ingredients`, `event_food_items` join/collection rows before removing the parent row.
- **Creator check uses `getId()` equality, not username** — `event.getCreator().getId().equals(currentUser.getId())` in `EventService.updateEvent` and `deleteEvent`. IDs are `Integer` — use `.equals()`, not `==`.

### Phase 3 — Sharing & discovery (added 2026-05-01)

- New env var: `FRONTEND_BASE_URL` (default `http://localhost:8094`) is required for share-link composition. Add to compose `.env`; force-recreate the `app` container after editing. See [`docs/specs/2026-05-01-phase-3-sharing-discovery-design.md`](../party-docs/specs/2026-05-01-phase-3-sharing-discovery-design.md).
- New public endpoints: `GET /events/public`, `GET /events/share/{token}`. **`GET /events/{id}` is also now public** at the security layer — `EventService.getEvent` enforces creator-only access for private events. Order in `SecurityConfig` matters: GET-specific permitAll matchers must precede the catch-all `/events/**` authenticated rule.
- Share token: `event.share_token` is a nullable `CHAR(36)` UUID, lazily issued by `POST /events/{id}/share`, rotated by `POST /events/{id}/share/rotate`. The token is `@JsonIgnore`'d on the entity — never returned in any GET response, only emitted via `ShareLinkResponse`.

### Phase 3.5 — Guest identities, join requests, attendees (added 2026-05-01)

- New tables (V4): `guest_user`, `join_request`, `attendee`. Each with XOR `CHECK` constraints (`(user_id IS NULL) <> (guest_id IS NULL)`) so identity is exactly one of User/Guest.
- New header convention: **`X-Guest-Token: <uuid>`** carries guest identity from the FE. CORS allowed-headers (currently `*`) covers it. Both `Authorization: Bearer` and `X-Guest-Token` may be present on a single request — `CallerResolver` prefers the JWT.
- New `service/identity/` package: `CallerIdentity` sealed interface + `AuthenticatedUser`/`Guest` records. New service methods accept `CallerIdentity` (or `Optional<CallerIdentity>` for visibility-gated reads), uniform across user vs guest.
- **Visibility decisions live in services, not in `SecurityConfig`.** `GET /events/{id}/attendees` is `permitAll` at the security layer; `AttendeeService.listAttendees` walks the four-step check (public -> creator -> attendee row -> 403). Same pattern as Phase 3's `GET /events/{id}` privacy gate. `PUT /events/{id}/attendees/me` is also `permitAll` at the security layer so `X-Guest-Token` resolution can run inside the controller; the controller returns 401 itself if no caller resolves.
- `DiscriminatorGenerator` is behind an interface so tests can stub deterministic colliding values (`GuestUserServiceCollisionTest`). Default `SecureRandomDiscriminatorGenerator` returns `0001..9999`.
- **`GuestUserService.createNew` retry uses `Propagation.REQUIRES_NEW`** via a `@Lazy` self-reference. Once `saveAndFlush` throws on a uniqueness collision, Hibernate marks the session for rollback and refuses to flush again, so the retry must run in a fresh transaction. Side effect: integration tests for guest flows can NOT use `@Transactional` (the REQUIRES_NEW commit isn't visible to a subsequent read in the outer test snapshot under MySQL REPEATABLE READ); they rely on `@AfterEach` cleanup instead.
- Phase 5 will read `join_request.decided_at`, `attendee.created_at`/`updated_at` to fire notifications. **Don't squash transitions in this phase** — every status change updates an existing row's timestamp, never delete-then-insert.
- `Handler.HttpMessageNotReadableException -> 400` is now wired up. Invalid enum values in request bodies (e.g. `{"status":"PARTYING"}`) surface as 400 instead of 500.

### Phase 7 — Drink recipe surfacing (added 2026-05-02)

- **`DrinksService.getDrinksForIngredients` rewrite.** Replaces any-overlap matching with **alcohol-only** completeness scoring. Input is normalized (trim + lowercase). Non-alcoholic names in the input are silently dropped (the matcher is alcohol-only by design — see Phase 7 design decision #4). For each candidate `Drink`: compute `requiredAlcohols = ingredients.filter(isAlcoholic=true)`, skip if zero overlap with the picked set or if `requiredAlcohols` is empty (mocktail), then set `fullyMakeable` and `missingAlcoholicIngredients`. Sort: fully-makeable first, then missing-count ascending, then alphabetical. Capped at 50 (`MAX_SUGGESTIONS`).
- **`@Cacheable` removed from `getDrinksForIngredients`.** Result is now a function of the picked set, not a straight repository read; the cache key (a `List`) was already order-sensitive. The orphaned `drinks` cache region in `CacheConfig.java` is left in place — Phase 9 cleanup.
- **`Drink.ingredients` cascade changed to `CascadeType.ALL` + `orphanRemoval = true`** (was `CascadeType.PERSIST`). The 2-step save pattern used by integration tests (`save → set children → save`) merges on the second call, and `PERSIST` doesn't cascade through merge. `ALL` covers it. Production seed path (one-shot save) is unaffected; no API edits drinks after creation, so `orphanRemoval` has no current trigger. Be aware if you add a "delete a drink" or "edit drink ingredients" code path.
- **`GetDrinksResponseDrink` gained two fields:** `fullyMakeable: boolean` and `missingAlcoholicIngredients: List<String>`. Populated only by `GET /drinks?ingredients=...`; the bare `GET /drinks` leaves them at default values (`false` / `null`).
- **Removed:** `DrinkRepository.findDistinctByIngredientsIngredientNameIn`. Was only called by the old `getDrinksForIngredients`.
- **FE wire-format gotcha:** Spring's `@RequestParam List<String>` accepts comma-separated (`?ingredients=foo,bar`) or repeated (`?ingredients=foo&ingredients=bar`) but **not** PHP-style brackets (`?ingredients[]=foo`), which is what default axios sends. The FE explicitly comma-joins before sending. If you add another list-param endpoint, expect to do the same on the FE side.
- **Integration test pattern:** `DrinksControllerIntegrationTest` and `EventControllerDrinksIntegrationTest` both seed minimal Ingredient + Drink + DrinkIngredient data via the repos in `@BeforeEach`, exercise the endpoint via MockMvc, clean up via `@AfterEach`. `@DirtiesContext(AFTER_EACH_TEST_METHOD)` is used on the drinks IT to avoid context bleed; the events IT uses `@AfterEach deleteAll()` instead. Both extend `BaseIntegrationTest`.
- **TheCocktailDB seed has separate generic and branded ingredient rows** ("Vodka" → 20 matches, "Absolut Vodka" → 1 match). Not a code bug, a data-quality artifact. If a future polish wants to merge brand variants into their generic, it's an `Ingredient`-table migration, not a service change.

## Where to add things

| What                  | Where                                                                                                                                                                                          |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| New REST endpoint     | `api/<Resource>Controller.java` + `service/<Resource>Service.java` + DTO in `model/request/` and `model/response/`                                                                            |
| New entity            | `model/<Entity>.java` + `repo/<Entity>Repository.java` extending `JpaRepository`                                                                                                              |
| New external API client | `service/<api>/` with Feign client + caller + `config/<Api>FeignConfig.java`                                                                                                                |
| New scheduled job     | Method on `tasks/ScheduledTasks.java` with `@Scheduled(...)`                                                                                                                                  |
| New exception type    | Subclass `ResourceException` (or a new bespoke type) + handler in `exception/Handler.java`                                                                                                    |

## Roadmap

`/home/dakiman/projects/party-docs/specs/2026-04-28-party-app-roadmap-design.md`
