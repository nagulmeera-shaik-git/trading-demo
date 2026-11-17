# Phase 1: market-data-service

Goal: a working Spring Boot service that generates live prices, serves them over REST,
and publishes every price change to Kafka.

## What was added

| File | What it does |
|---|---|
| `MarketDataServiceApplication` | Now has `@ConfigurationPropertiesScan`, so `MockFeedProperties` is picked up |
| `config/MockFeedProperties` | Reads `market.mock.*` from application.yml into a type-safe record |
| `config/SchedulingConfig` | Turns on `@Scheduled` methods |
| `config/KafkaTopicConfig` | Creates the `market.ticks` topic (6 partitions) at startup |
| `domain/PriceSnapshot` | Latest price data for one share, in paise |
| `domain/PriceStore` | Thread-safe in-memory map of latest prices |
| `feed/PriceFeed` | Interface for any price source (mock now, Kite in Phase 7) |
| `feed/MockPriceFeed` | Moves every price by up to 0.1% every 500 ms |
| `messaging/TickPublisher` | Sends each tick to Kafka, keyed by symbol |
| `api/PriceController` | REST endpoints |
| `api/PriceResponse` | API format: rupees with 2 decimals, plus change and change % |
| `api/ApiExceptionHandler` | Clean JSON 404 for unknown symbols |
| `application.yml` | Added `max.block.ms: 2000` so the feed doesn't freeze for 60 s when Kafka is down |
| `MockPriceFeedTest` | Unit test of the price rules (no Spring) |
| `PriceControllerTest` | Web-layer test with MockMvc |

## How to copy into your project

Unzip over your project folder (the zip has the same folder structure). It only adds or
replaces files inside `market-data-service/` and adds `docs/PHASE1.md`.
Your `docker-compose.yml` and other edits are not touched.

## How to run

1. Infrastructure must be running:
   ```
   podman compose up -d
   ```
2. Build and install the shared `common` module once (other modules depend on it):
   ```
   mvn -pl common install
   ```
3. Run the tests:
   ```
   mvn -pl market-data-service test
   ```
4. Start the service:
   ```
   mvn -pl market-data-service spring-boot:run
   ```
   Or in Eclipse: right-click `MarketDataServiceApplication` -> Run As -> Java Application
   (or Spring Boot App if you have Spring Tools installed).

You should see in the logs:
`Mock price feed ready for 10 instruments, tick every 500 ms`

## How to check it works

```
curl http://localhost:8082/market/instruments
curl http://localhost:8082/market/prices/INFY
curl http://localhost:8082/market/prices
curl http://localhost:8082/market/prices/XYZ
curl http://localhost:8082/actuator/health
```

Call `/market/prices/INFY` a few times: `ltp`, `high`, `low` and `volume` change each time.
`XYZ` returns a 404 with a JSON error.

Kafka: open http://localhost:8090 -> Topics -> `market.ticks` -> Messages.
You'll see new messages arriving every half second. Click one: the key is the symbol and the value
is the JSON TickEvent. Notice each symbol always lands on the same partition.

Through the gateway (optional): start `api-gateway` the same way, then
```
curl http://localhost:8080/api/market/prices/INFY
```

## Done when

- [ ] Both tests pass
- [ ] `/market/prices/INFY` shows a different price on each call
- [ ] `market.ticks` in Kafka UI shows messages arriving continuously
- [ ] Changes committed to Git

## Try these (learning exercises)

1. Change `tick-interval-ms` to 2000 in application.yml, restart, and see the slower updates.
2. Add a new instrument (e.g. `BAJFINANCE: 7000.00`) without changing any Java code.
3. Stop Kafka (`podman compose stop kafka`). The REST API keeps working; the logs show
   "Could not publish tick". Start Kafka again and publishing resumes on its own.
   This is why services shouldn't depend on each other being up.
4. In Kafka UI, look at which partition INFY's messages go to. Restart the service:
   INFY still goes to the same partition. That's the effect of the message key.
5. Write a test for `PriceResponse.from()` with an open price of 0.

## Concepts covered

Spring Boot auto-configuration, `@ConfigurationProperties` with records, profiles (`mock`),
`@Scheduled`, thread safety with `ConcurrentHashMap` and immutable records, Kafka producer with
keys and partitions, topic creation with `NewTopic`, REST controllers, `ProblemDetail` error
handling, unit tests with Mockito, and web slice tests with `@WebMvcTest` and `@MockitoBean`.
