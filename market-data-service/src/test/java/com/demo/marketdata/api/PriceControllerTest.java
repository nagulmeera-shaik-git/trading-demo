package com.demo.marketdata.api;

import com.demo.marketdata.domain.PriceSnapshot;
import com.demo.marketdata.domain.PriceStore;
import com.demo.marketdata.feed.PriceFeed;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web layer test: starts only the MVC part of Spring, with the store and feed mocked. */
@WebMvcTest(PriceController.class)
class PriceControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PriceStore store;

    @MockitoBean
    private PriceFeed feed;

    @Test
    void returnsPriceInRupees() throws Exception {
        when(store.find("INFY")).thenReturn(Optional.of(new PriceSnapshot(
                "INFY", 150_250, 150_245, 150_255, 150_000, 150_300, 149_900, 1_234,
                Instant.parse("2026-11-16T10:00:00Z"))));

        mvc.perform(get("/market/prices/infy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("INFY"))
                .andExpect(jsonPath("$.ltp").value(1502.50))
                .andExpect(jsonPath("$.change").value(2.50))
                .andExpect(jsonPath("$.changePercent").value(0.17))
                .andExpect(jsonPath("$.volume").value(1234));
    }

    @Test
    void unknownSymbolReturns404() throws Exception {
        when(store.find("XYZ")).thenReturn(Optional.empty());

        mvc.perform(get("/market/prices/XYZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Unknown symbol"));
    }

    @Test
    void listsInstrumentsSorted() throws Exception {
        when(feed.symbols()).thenReturn(Set.of("TCS", "INFY"));

        mvc.perform(get("/market/instruments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("INFY"))
                .andExpect(jsonPath("$[1]").value("TCS"));
    }
}
