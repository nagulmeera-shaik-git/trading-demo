package com.demo.marketdata.feed;

import com.demo.marketdata.config.MockFeedProperties;
import com.demo.marketdata.domain.PriceSnapshot;
import com.demo.marketdata.domain.PriceStore;
import com.demo.marketdata.messaging.TickPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/** Plain unit test: no Spring, no Kafka. Fast and focused on the price rules. */
class MockPriceFeedTest {

    private PriceStore store;
    private TickPublisher publisher;
    private MockPriceFeed feed;

    @BeforeEach
    void setUp() {
        var properties = new MockFeedProperties(500, Map.of(
                "INFY", new BigDecimal("1500.00"),
                "TCS", new BigDecimal("3900.00")));
        store = new PriceStore();
        publisher = mock(TickPublisher.class);
        feed = new MockPriceFeed(properties, store, publisher);
    }

    @Test
    void loadsStartingPricesFromConfiguration() {
        assertThat(feed.symbols()).containsExactlyInAnyOrder("INFY", "TCS");
        assertThat(store.find("INFY")).get().extracting(PriceSnapshot::ltp).isEqualTo(150_000L);
        assertThat(store.find("tcs")).get().extracting(PriceSnapshot::ltp).isEqualTo(390_000L);
    }

    @Test
    void everyTickFollowsThePriceRules() {
        for (int i = 0; i < 1_000; i++) {
            feed.tick();
        }

        for (PriceSnapshot s : store.all()) {
            assertThat(s.ltp() % MockPriceFeed.TICK_SIZE).as("ltp on tick size").isZero();
            assertThat(s.bid()).as("bid below ltp").isLessThan(s.ltp());
            assertThat(s.ask()).as("ask above ltp").isGreaterThan(s.ltp());
            assertThat(s.ltp()).isBetween(s.low(), s.high());
            assertThat(s.volume()).isPositive();
        }
        verify(publisher, times(2_000)).publish(any());
    }

    @Test
    void roundsToFivePaise() {
        assertThat(MockPriceFeed.roundToTick(150_002)).isEqualTo(150_000);
        assertThat(MockPriceFeed.roundToTick(150_003)).isEqualTo(150_005);
    }
}
