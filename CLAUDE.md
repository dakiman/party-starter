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

- **Schema is currently managed by `ddl-auto: update`** — Flyway migration is Phase 1 work. Until Flyway lands, manual schema edits will be silently overwritten on app boot.
- **JWT secret default is `MYJWTSECRET123`** — fail-fast on missing env var is Phase 1 work. Production must set `JWT_SECRET`.
- **CORS allows all origins** (`SecurityConfig#corsConfigurationSource`) — tightening to env-driven origin list is Phase 1 work.
- **`/drinks`, `/ingredients`, `/music/**` are unauthenticated** — `// TODO protect endpoint` comments in `SecurityConfig`. Decision pending in Phase 1.
- **Spotify `client-id` is hardcoded** in `application.yml`. Move to env var in Phase 1.
- **No tests for `EventService` / `EventController`**. Phase 1 work — adds Testcontainers + integration test base.
- **`MusicController` has commented-out `/recommendations`** endpoint — kept for reference; properly wired in Phase 8 (Spotify playlist generation).
- **`PostEventRequest.drinks: List<Integer>`** is sent as `[]` from FE always — historical artifact; removed in Phase 9 polish.

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
