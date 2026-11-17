package com.demo.marketdata.feed;

import com.demo.marketdata.config.MockFeedProperties;
import com.demo.marketdata.domain.PriceSnapshot;
import com.demo.marketdata.domain.PriceStore;
import com.demo.marketdata.messaging.TickPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates realistic-looking random prices so you can develop at any time,
 * even when the real market is closed.
 *
 * Each tick, every price moves randomly by up to 0.1%, rounded to the 5-paise tick size.
 * Active only with the "mock" Spring profile.
 */
@Component
@Profile("mock")
public class MockPriceFeed implements PriceFeed {

    private static final Logger log = LoggerFactory.getLogger(MockPriceFeed.class);

    static final long TICK_SIZE = 5;          // 5 paise = Rs 0.05
    static final double MAX_MOVE = 0.001;      // +/- 0.1% per tick

    private final PriceStore store;
    private final TickPublisher publisher;
    private final Set<String> symbols;
    // only touched by the single scheduler thread, so a plain map is fine
    private final Map<String, PriceSnapshot> state = new LinkedHashMap<>();

    public MockPriceFeed(MockFeedProperties properties, PriceStore store, TickPublisher publisher) {
        this.store = store;
        this.publisher = publisher;

        Instant now = Instant.now();
        properties.instruments().forEach((symbol, rupees) -> {
            long price = roundToTick(toPaise(rupees));
            PriceSnapshot first = new PriceSnapshot(symbol, price, price - TICK_SIZE, price + TICK_SIZE,
                    price, price, price, 0, now);
            state.put(symbol, first);
            store.update(first);
        });
        this.symbols = Set.copyOf(state.keySet());
        log.info("Mock price feed ready for {} instruments, tick every {} ms",
                symbols.size(), properties.tickIntervalMs());
    }

    @Override
    public Set<String> symbols() {
        return symbols;
    }

    /** Called by Spring every tick-interval-ms. Moves every price once and publishes it. */
    @Scheduled(fixedRateString = "${market.mock.tick-interval-ms:500}")
    public void tick() {
        Instant now = Instant.now();
        state.replaceAll((symbol, previous) -> {
            PriceSnapshot next = nextSnapshot(previous, now);
            store.update(next);
            publisher.publish(next);
            return next;
        });
    }

    PriceSnapshot nextSnapshot(PriceSnapshot prev, Instant now) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        double move = (random.nextDouble() * 2 - 1) * MAX_MOVE;           // between -0.1% and +0.1%
        long ltp = Math.max(TICK_SIZE, roundToTick(Math.round(prev.ltp() * (1 + move))));

        long spread = TICK_SIZE * (1 + random.nextInt(3));               // 5 to 15 paise
        long bid = Math.max(TICK_SIZE, ltp - spread);
        long ask = ltp + spread;

        long volume = prev.volume() + 1 + random.nextInt(500);

        return new PriceSnapshot(prev.symbol(), ltp, bid, ask, prev.open(),
                Math.max(prev.high(), ltp), Math.min(prev.low(), ltp), volume, now);
    }

    static long roundToTick(long paise) {
        return Math.round(paise / (double) TICK_SIZE) * TICK_SIZE;
    }

    private static long toPaise(BigDecimal rupees) {
        return rupees.movePointRight(2).longValue();
    }
}
