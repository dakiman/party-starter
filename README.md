# Party Starter

Spring Boot REST API backing the [Party Buddy](https://github.com/dakiman/party-buddy) Vue frontend. Lets users plan parties — time/place, music (Spotify), drinks (TheCocktailDB), and food.

## Stack

- Spring Boot 3.2.3 / Java 21 / Maven
- Spring Data JPA + MySQL 8
- Spring Security + JWT (auth0 java-jwt)
- Spring Cloud OpenFeign (Spotify, TheCocktailDB clients)
- Lombok, SpringDoc OpenAPI, Spring Cache

## Prerequisites

- Java 21 (Temurin recommended)
- Docker + docker compose (for MySQL — or run MySQL on host)
- Maven (the wrapper `./mvnw` is included)

## Local development

The simplest path is fully containerized:

```bash
cp .env.example .env   # only needed once
docker compose up -d
docker compose logs -f app
```

The app is then available on `http://localhost:${APP_PORT:-8093}`. Swagger UI: `/swagger-ui/index.html`.

For a faster iteration loop with the JVM running on host (and only MySQL in Docker):

```bash
docker compose up -d mysql            # exposes MySQL at 127.0.0.1:3308
DB_HOST=localhost:3308 \
  DB_PASSWORD=changeme \
  ./mvnw spring-boot:run              # default port 8080
```

(Or `set -a; source .env; set +a; ./mvnw spring-boot:run` to load every var from `.env`.)

Swagger UI for the host-JVM mode: http://localhost:8080/swagger-ui/index.html

## Environment variables

| Variable                | Default                                                | Notes                                                                                         |
|-------------------------|--------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| `DB_HOST`               | `localhost:3306`                                       | MySQL `host:port`. For the bundled compose's MySQL: `localhost:3308` (host-JVM mode) or `mysql:3306` (containerized mode — set automatically by compose). |
| `DB_NAME`               | `spring_app`                                           | Database name                                                                                 |
| `DB_USERNAME`           | `root`                                                 |                                                                                               |
| `DB_PASSWORD`           | (empty)                                                |                                                                                               |
| `JWT_SECRET`            | — (REQUIRED)                                           | No default. Boot fails fast if missing or shorter than 32 chars.                              |
| `JWT_ISSUER`            | `PartyStarterInc`                                      |                                                                                               |
| `CORS_ALLOWED_ORIGINS`  | `http://localhost:5173,http://localhost:8094`          | Comma-separated allowed origins for CORS                                                      |
| `SHOULD_SEED`           | `false`                                                | Enable the periodic CocktailDB seed job                                                       |
| `COCKTAIL_DB_API_KEY`   | `1`                                                    | TheCocktailDB free key                                                                        |
| `SPOTIFY_CLIENT_ID`     | `d0982bc6c139493bbe74eee8d2ddd811` (existing public id)| Public client id for Spotify Web API                                                          |
| `SPOTIFY_SECRET`        | —                                                      | Required for Spotify integration                                                              |
| `LOGGING_LEVEL`         | `info`                                                 |                                                                                               |

Copy `.env.example` to `.env` for local docker compose.

## Deployment

Deployed on the user's personal server (`dakis-server`) via Docker. The repo's `docker-compose.yml` is brought up independently — it is **not** included in `dakis-server/docker-compose.yml` (the FE is, but the BE keeps its own MySQL on a private bridge network).

See the roadmap spec for the full deploy story:
`/home/dakiman/projects/party-docs/specs/2026-04-28-party-app-roadmap-design.md`

The previous VPS deploy workflow (`.github/workflows/backend-deploy.yml`) has been removed.

## Project layout

```
src/main/java/com/example/partystarter/
├── PartystarterApplication.java   # Spring Boot entry point
├── api/                           # REST controllers
├── service/                       # Business logic
│   ├── spotify/                   # Spotify OpenFeign client + caller
│   └── cocktail/                  # TheCocktailDB OpenFeign client + caller
├── repo/                          # JPA repositories
├── model/                         # Entities + request/response DTOs
├── security/                      # JWT auth (filter, util, security config)
├── config/                        # Spring config beans (cache, OpenAPI, Feign)
├── exception/                     # @ControllerAdvice + custom exceptions
├── tasks/                         # @Scheduled jobs
└── utils/                         # Convert/Reflection utilities
```

## Conventions and gotchas

See [`CLAUDE.md`](./CLAUDE.md).

## Roadmap

`/home/dakiman/projects/party-docs/specs/2026-04-28-party-app-roadmap-design.md`
