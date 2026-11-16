# trading-demo

A Kite-style paper trading platform built to learn Spring Boot microservices step by step.
Real or mock prices, simulated order execution, no real money involved.

## Prerequisites

- Java 21 (Temurin or Corretto)
- Maven 3.9+
- Docker Desktop (or Docker Engine + Compose plugin)
- An IDE (IntelliJ IDEA recommended)

## Services

| Module | Port | Database | Purpose |
|---|---|---|---|
| `common` | - | - | Shared event records, Kafka topic names, header constants |
| `api-gateway` | 8080 | - | Single entry point: routing, JWT check (Phase 3), correlation ID, rate limits |
| `auth-service` | 8081 | auth_db | Register and login users, issue JWT tokens |
| `market-data-service` | 8082 | - | Produces live prices: mock generator (default) or Zerodha Kite (Phase 7) |
| `watchlist-service` | 8083 | watchlist_db | User watchlists, enriched with latest prices |
| `order-service` | 8084 | order_db | Paper orders, order state machine and the simulated fill engine |
| `portfolio-service` | 8085 | portfolio_db | Holdings, demo funds and P&L, updated from fill events |
| `stream-service` | 8086 | - | Pushes live prices and order updates to browsers over WebSocket |

## Local infrastructure

| Tool | URL | Login |
|---|---|---|
| PostgreSQL | localhost:5432 | trading / trading |
| Redis | localhost:6379 | - |
| Kafka | localhost:9092 | - |
| Kafka UI | http://localhost:8090 | - |
| Zipkin (tracing) | http://localhost:9411 | - |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin / admin |

## Getting started

```bash
# 1. Start infrastructure
docker compose up -d

# 2. (Optional) add the Maven wrapper so others don't need Maven installed
mvn -N wrapper:wrapper

# 3. Build everything
mvn clean install

# 4. Run a service (or run the *Application class from your IDE)
mvn -pl market-data-service spring-boot:run

# 5. Check it is up
curl http://localhost:8082/actuator/health
```

All client traffic goes through the gateway on port 8080:
`/api/auth/**`, `/api/market/**`, `/api/watchlists/**`, `/api/orders/**`, `/api/portfolio/**`, and WebSocket `/ws/**`.

## Conventions

- **Money** is stored in paise as `long` / `BIGINT`. Never `double`.
- **One database per service.** Services share data only through REST calls or Kafka events.
- **Schema changes** go in Flyway migrations: `src/main/resources/db/migration/V<n>__description.sql`.
- **Events** live in `common` (`com.demo.common.events`). Change them carefully: every consumer depends on them.
- **Kafka keys**: `market.ticks` and `orders.placed` keyed by symbol; `orders.filled` keyed by userId.
- **Package layout** in every service: `api`, `domain`, `service`, `repository`, `messaging`, `config`.

## Learning roadmap

- [ ] Phase 1: market-data-service with mock price feed and REST endpoint
- [ ] Phase 2: watchlist-service with PostgreSQL, JPA, Flyway, Testcontainers
- [ ] Phase 3: auth-service (JWT) and api-gateway security
- [ ] Phase 4: Kafka ticks, stream-service WebSocket, live watchlist page
- [ ] Phase 5: order-service with fill engine, portfolio-service from fill events
- [ ] Phase 6: correlation IDs, Resilience4j, metrics, tracing, dashboards
- [ ] Phase 7: real prices from Zerodha Kite Connect (`kite` profile)
- [ ] Phase 8: Docker images, local Kubernetes (kind or minikube)
- [ ] Phase 9: stretch goals (own matching engine, outbox pattern, CI, AWS EKS)
