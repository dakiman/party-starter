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
- **`PostEventRequest.drinks: List<Integer>`** is sent as `[]` from FE always — historical artifact; removed in Phase 9 polish.
- **`EventResponse` includes `creatorUsername`** — populated from `event.getCreator().getUsername()` in `ConvertUtils.mapEventToResponse`. Required by the FE to gate Edit/Delete button visibility. Null-safe: if `creator` is null (should not happen in production), the field is `null`.
- **`PUT /events/{id}` is a full replace** — all association lists (artists, drinks, ingredients, food) are replaced wholesale, same as create. There is no `PATCH` partial-update endpoint.
- **`DELETE /events/{id}` uses `eventRepository.delete(entity)`** — not `deleteById`. Must pass the managed entity so JPA cascade rules fire and clear `event_artists`, `event_drinks`, `event_ingredients`, `event_food_items` join/collection rows before removing the parent row.
- **Creator check uses `getId()` equality, not username** — `event.getCreator().getId().equals(currentUser.getId())` in `EventService.updateEvent` and `deleteEvent`. IDs are `Integer` — use `.equals()`, not `==`.

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
