# Phase 2: watchlist-service

Goal: a service with its own PostgreSQL database that manages users' watchlists and shows
live prices by calling market-data-service.

## What you learn

JPA entities and repositories, Flyway migrations, transactions, request validation,
global error handling, calling another service with RestClient (with timeouts),
graceful degradation when a dependency is down, and integration tests with Testcontainers.

## What was added

| File | What it does |
|---|---|
| `domain/Watchlist` | JPA entity for the `watchlists` table, with business rules (max 50 symbols, no duplicates) |
| `domain/WatchlistItem` | One symbol in a watchlist; a row in `watchlist_items` |
| `repository/WatchlistRepository` | Spring Data repository; SQL generated from method names |
| `client/MarketDataClient` | Calls market-data-service: all prices, and "does this symbol exist?" |
| `client/MarketDataClientProperties` | URL and timeouts from application.yml |
| `config/RestClientConfig` | Builds the RestClient with 1 s connect / 2 s read timeouts |
| `service/WatchlistService` | Use cases and rules (max 7 watchlists, owner checks) |
| `api/WatchlistController` | REST endpoints |
| `api/*Request`, `*Summary`, `*Detail`, `*ItemView` | Request and response shapes (DTOs) |
| `api/ApiExceptionHandler` | Turns exceptions into 400 / 404 / 409 / 503 JSON errors |
| `application.yml` | Adds `clients.market-data` settings |
| `WatchlistTest` | Unit test of entity rules |
| `WatchlistApiIntegrationTest` | Full test with real PostgreSQL in a container |

The database tables already exist in `src/main/resources/db/migration/V1__init.sql`.
Flyway creates them the first time the service starts.

## API

| Method | Path | Body | Result |
|---|---|---|---|
| GET | `/watchlists` | - | Your watchlists (no prices) |
| POST | `/watchlists` | `{"name":"Banks"}` | 201, new watchlist |
| GET | `/watchlists/{id}` | - | Watchlist with live prices |
| PUT | `/watchlists/{id}` | `{"name":"New name"}` | Renamed watchlist |
| DELETE | `/watchlists/{id}` | - | 204 |
| POST | `/watchlists/{id}/items` | `{"symbol":"INFY"}` | 201, watchlist with prices |
| DELETE | `/watchlists/{id}/items/{symbol}` | - | 204 |

User: the `X-User-Id` header. If missing, user 1 is used (until Phase 3 adds login).

## How to run

1. Infrastructure: `podman compose up -d`
2. Install shared modules if you changed `common`: `mvn -pl common -am install`
3. Tests: `mvn -pl watchlist-service test` (needs Podman running; see Testcontainers note below)
4. Start market-data-service (Phase 1) so prices are available.
5. Start watchlist-service: `mvn -pl watchlist-service spring-boot:run`
   or run `WatchlistServiceApplication` from Eclipse.

On first start the log shows Flyway applying `V1__init`. In DBeaver, `watchlist_db` now has
`watchlists`, `watchlist_items` and `flyway_schema_history`.

## Try it (Windows Command Prompt)

```
curl -X POST http://localhost:8083/watchlists -H "Content-Type: application/json" -d "{\"name\":\"IT stocks\"}"
curl -X POST http://localhost:8083/watchlists/1/items -H "Content-Type: application/json" -d "{\"symbol\":\"INFY\"}"
curl -X POST http://localhost:8083/watchlists/1/items -H "Content-Type: application/json" -d "{\"symbol\":\"TCS\"}"
curl http://localhost:8083/watchlists/1
curl http://localhost:8083/watchlists
```

Postman or Bruno is easier for POST requests than curl on Windows.

Call `GET /watchlists/1` a few times: the prices change, because they come live from market-data-service.

## Testcontainers with Podman on Windows

The integration test starts its own PostgreSQL container. If the test fails with
"Could not find a valid Docker environment":

1. In Podman Desktop: Settings -> Preferences -> enable **Docker Compatibility**
   (makes Podman answer on Docker's address).
2. If it then fails on a container named "ryuk", set this environment variable and reopen the terminal:
   ```
   setx TESTCONTAINERS_RYUK_DISABLED true
   ```
   (Clean up leftover test containers occasionally with `podman container prune`.)

## Done when

- [ ] All tests pass
- [ ] You can create a watchlist, add INFY and TCS, and see their live prices
- [ ] You see the rows in DBeaver
- [ ] Changes committed to Git

## Try these (learning exercises)

1. **Stop market-data-service** and call `GET /watchlists/1`. You still get your symbols, with
   `"pricesAvailable": false`. Adding a symbol returns 503. Start it again: prices come back.
   This is graceful degradation.
2. **Turn on SQL logging**: set `org.hibernate.SQL: DEBUG` in application.yml. Add a symbol and read
   the SQL. Notice Hibernate deletes and re-inserts the item rows: that's how element collections
   update. Fine for 50 rows; think about when it wouldn't be.
3. **Try another user**: send `-H "X-User-Id: 2"` and confirm you can't see user 1's watchlists.
4. **Add a migration**: create `V2__add_watchlist_color.sql` with
   `ALTER TABLE watchlists ADD COLUMN color VARCHAR(7);` and map it in the entity.
   Never edit V1 after it has run; always add a new file.
5. **Cache prices** with Redis using `@Cacheable` on `MarketDataClient.latestPrices()` for 1 second.
6. **Question to think about**: `addSymbol` calls market-data while a database transaction is open.
   Why could that be a problem under heavy load, and how would you change it?

## Concepts covered

Entities, element collections, repositories with derived queries, Flyway, `ddl-auto: validate`,
read-only vs write transactions and dirty checking, Bean Validation, ProblemDetail errors,
RestClient with timeouts, ownership checks (404 instead of 403), graceful degradation,
Testcontainers with `@ServiceConnection`, and `@MockitoBean` for other services.
