package com.demo.marketdata.domain;

import java.time.Instant;

/**
 * The latest known price data for one share. All prices are in paise (1 rupee = 100 paise),
 * stored as long so there are never rounding errors.
 */
public record PriceSnapshot(
        String symbol,
        long ltp,       // last traded price
        long bid,       // best price a buyer is offering
        long ask,       // best price a seller is asking
        long open,      // first price of the session
        long high,      // highest price so far
        long low,       // lowest price so far
        long volume,    // shares traded so far
        Instant timestamp) {
}
