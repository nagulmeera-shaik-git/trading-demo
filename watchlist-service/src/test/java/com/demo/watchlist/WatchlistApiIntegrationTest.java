package com.demo.watchlist;

import com.demo.watchlist.client.MarketDataClient;
import com.demo.watchlist.client.MarketDataUnavailableException;
import com.demo.watchlist.client.PriceView;
import com.demo.watchlist.repository.WatchlistRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full integration test: real Spring context, real PostgreSQL in a container (Testcontainers),
 * real Flyway migrations. Only market-data-service is replaced with a mock.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class WatchlistApiIntegrationTest {

    // Full image name so Podman doesn't need to ask which registry to use
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("docker.io/library/postgres:16").asCompatibleSubstituteFor("postgres"));

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WatchlistRepository repository;

    @MockitoBean
    private MarketDataClient marketData;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void createsAndListsWatchlists() throws Exception {
        createWatchlist(1, "Banks");
        createWatchlist(1, "IT");

        mvc.perform(get("/watchlists").header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Banks"))
                .andExpect(jsonPath("$[0].position").value(1))
                .andExpect(jsonPath("$[1].position").value(2));
    }

    @Test
    void addedSymbolShowsLivePrice() throws Exception {
        long id = createWatchlist(1, "IT");
        when(marketData.symbolExists("INFY")).thenReturn(true);
        when(marketData.latestPrices()).thenReturn(Map.of("INFY",
                new PriceView("INFY", new BigDecimal("1500.60"), new BigDecimal("0.60"), new BigDecimal("0.04"))));

        mvc.perform(post("/watchlists/{id}/items", id).header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"infy\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pricesAvailable").value(true))
                .andExpect(jsonPath("$.items[0].symbol").value("INFY"))
                .andExpect(jsonPath("$.items[0].ltp").value(1500.60));
    }

    @Test
    void rejectsUnknownSymbol() throws Exception {
        long id = createWatchlist(1, "IT");
        when(marketData.symbolExists("XYZ")).thenReturn(false);

        mvc.perform(post("/watchlists/{id}/items", id).header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"XYZ\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Unknown symbol"));
    }

    @Test
    void rejectsDuplicateSymbol() throws Exception {
        long id = createWatchlist(1, "IT");
        when(marketData.symbolExists("TCS")).thenReturn(true);
        when(marketData.latestPrices()).thenReturn(Map.of());
        addSymbol(id, "TCS");

        mvc.perform(post("/watchlists/{id}/items", id).header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"TCS\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void allowsAtMostSevenWatchlists() throws Exception {
        for (int i = 1; i <= 7; i++) {
            createWatchlist(1, "List " + i);
        }

        mvc.perform(post("/watchlists").header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"One too many\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void usersCannotSeeEachOthersWatchlists() throws Exception {
        long id = createWatchlist(1, "Private");
        when(marketData.latestPrices()).thenReturn(Map.of());

        mvc.perform(get("/watchlists/{id}", id).header("X-User-Id", 2))
                .andExpect(status().isNotFound());
    }

    @Test
    void stillWorksWhenMarketDataIsDown() throws Exception {
        long id = createWatchlist(1, "IT");
        when(marketData.symbolExists("INFY")).thenReturn(true);
        when(marketData.latestPrices()).thenReturn(Map.of());
        addSymbol(id, "INFY");

        when(marketData.latestPrices()).thenThrow(new MarketDataUnavailableException("down", null));

        mvc.perform(get("/watchlists/{id}", id).header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pricesAvailable").value(false))
                .andExpect(jsonPath("$.items[0].symbol").value("INFY"))
                .andExpect(jsonPath("$.items[0].ltp").value(nullValue()));
    }

    @Test
    void validatesRequestBody() throws Exception {
        mvc.perform(post("/watchlists").header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    private long createWatchlist(long userId, String name) throws Exception {
        String body = mvc.perform(post("/watchlists").header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private void addSymbol(long watchlistId, String symbol) throws Exception {
        mvc.perform(post("/watchlists/{id}/items", watchlistId).header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"" + symbol + "\"}"))
                .andExpect(status().isCreated());
    }
}
