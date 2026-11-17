package com.demo.marketdata.feed;

import java.util.Set;

/**
 * A source of live prices. Phase 1 has one implementation (MockPriceFeed).
 * Phase 7 adds KitePriceFeed for real NSE prices; the rest of the service doesn't change.
 */
public interface PriceFeed {

    /** Symbols this feed produces prices for. */
    Set<String> symbols();
}
