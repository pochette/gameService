# Game Service

> REST service for storing player state, activity events and analytics on top of them.

![Java](https://img.shields.io/badge/Java_21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4.1-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_17-4169E1?logo=postgresql&logoColor=white)
![Liquibase](https://img.shields.io/badge/Liquibase-2962FF?logo=liquibase&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Testcontainers-291A3F?logo=testcontainers&logoColor=white)
![Docker](https://img.shields.io/badge/Docker_Compose-2496ED?logo=docker&logoColor=white)

[Русский](README.md) · **English**

## About

A game backend built to a technical specification. The client stores an arbitrary JSON player state and sends activity events. The service returns event history for a period, the top players of a country by balance, and the number of new users.

**Key decisions:**

- **Flexible schema, fast analytics.** The full state is stored as `JSONB`, unknown fields included. Balance and country are extracted into indexed columns. Filtering, sorting and counting run in PostgreSQL, not in memory.
- **Concurrent-write race fixed.** The "check if user exists, then insert" sequence was replaced with a single atomic `INSERT ... ON CONFLICT DO UPDATE`. The behaviour is covered by an integration test.
- **Uniform error format.** All errors are returned as `{"code","message"}` with proper 400/404/409/413 statuses.
- **Reproducible environment.** Docker Compose starts everything with one command, Liquibase migrations create the schema, and tests start PostgreSQL via Testcontainers.

## Tech stack

| Area | Technologies |
| --- | --- |
| Language and framework | Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Validation) |
| Data | PostgreSQL 17 (JSONB), Liquibase |
| API docs | springdoc-openapi, Swagger UI |
| Testing | JUnit 5, Mockito, Testcontainers, Postman |
| Build and run | Maven Wrapper, Docker, Docker Compose |

## Architecture

Packages are split by domain; each domain has controller → service → repository layers.

```text
src/main/java/ru/burdak/game
├── user/         # player state, country top
├── activity/     # activity events
├── statistic/    # analytics: new users
└── common/       # error handling, validation, JSON config
src/main/resources/db/changelog   # Liquibase migrations
```

## Quick start

### Docker Compose

Requires Docker Desktop and a free port 8080.

```bash
docker compose up --build -d
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

Stop with `docker compose down`; add `-v` to also remove the data volume.

### Locally

Requires JDK 21 and PostgreSQL with a `game_service` database on `localhost:5432` (user and password `postgres`).

```bash
./mvnw clean package -DskipTests
java -jar target/game-0.0.1-SNAPSHOT.jar
```

Override the connection with `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`.

## API

| Method and path | Purpose | 200 response |
| --- | --- | --- |
| `PUT /users/{userId}/state` | Create or fully replace the state | Empty |
| `GET /users/{userId}/state` | Get the state | `userId`, `state` |
| `POST /users/{userId}/activities` | Record an event | Empty |
| `GET /users/{userId}/activities?from=&to=` | Events for `[from, to)` | Array of `userId`, `activity`, `receivedAt` |
| `GET /users/top?country=&limit=` | Country top by balance | Array of `userId`, `money` |
| `GET /statistics/new-users?country=&from=&to=` | New users for a period | Number |

Errors: **400** invalid request, **404** `USER_NOT_FOUND`, **409** `COUNTRY_IMMUTABLE`, **413** `STATE_TOO_LARGE`.

<details>
<summary><b>Detailed contract</b></summary>

- `userId` is a UUID. The state is a JSON object with required `money` (`long`) and `country` (1–255 Latin letters, case-sensitive).
- State size is limited to 10,240 bytes in UTF-8. Unknown fields, including nested ones, are preserved.
- The first PUT creates the user. A repeated PUT replaces the whole state, keeping the country and creation time. Changing the country is rejected without modifying data.
- `activity` is a required `long`. Each POST creates a separate event with server receive time.
- `from` and `to` are ISO 8601 with offset, `from < to`. Encode `+` in the URL as `%2B`.
- `limit` is 1–100. Top sorting: `money` descending, then UUID ascending.
- Empty results return `[]`, counts return `0`. A repeated PUT does not increase the new-user count.

</details>

## Data storage

| Table | Columns |
| --- | --- |
| `users` | `id UUID` PK, `money BIGINT`, `country VARCHAR(255)`, `state JSONB`, `created_at TIMESTAMPTZ` |
| `activities` | `id BIGINT IDENTITY` PK, `user_id UUID` FK → `users.id`, `activity BIGINT`, `received_at TIMESTAMPTZ` |

| Index | Purpose |
| --- | --- |
| `users(country, money DESC, id ASC)` | Country top already in the required order |
| `users(country, created_at)` | New users of a country for a period |
| `activities(user_id, received_at)` | User events for a period |

### Concurrent writes

Testing revealed a race: two simultaneous first PUTs for the same UUID both saw no record and both tried to create it. Wrapping "check, then save" in a transaction did not help.

The fix is an atomic UPSERT, `INSERT ... ON CONFLICT (id) DO UPDATE`, conditioned on a matching country. The database resolves the conflict, and balance and JSON are updated consistently. The `shouldSaveSameUserConcurrently` test fires two parallel PUTs and checks that the final state matches one of the requests in full.

## Testing

```bash
# Unit and integration tests (PostgreSQL in Testcontainers, Docker required)
./mvnw "-Dtest=UserServiceTest,UserApiIntegrationTest" test

# Full suite (contextLoads additionally needs a local database)
./mvnw test
```

- **JUnit + Testcontainers:** 11 tests — 4 unit, 6 integration, 1 context check. They cover creation, state replacement with unknown fields, validation, the country-change ban, the top query and concurrent saves.
- **Postman:** a [collection](postman/game-service.postman_collection.json) of 28 requests with automated checks for all six endpoints, including negative cases (400/404/409/413).

## Scope and limitations

The full mandatory scope of the specification is implemented. Deliberately out of scope:

- event pagination and cleanup of old statistics;
- separate automated tests for every error branch (some are covered only by the Postman collection);
- the optional load-estimation part.

## Author

**Andrew Burdak** — [GitHub](https://github.com/pochette) · [Telegram @burdak_aa](https://t.me/burdak_aa) · [burdak.andrew@gmail.com](mailto:burdak.andrew@gmail.com)
