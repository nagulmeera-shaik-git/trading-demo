package com.demo.common.events;

import java.time.Instant;

/**
 * One live price update for a share. Prices are in paise (1 rupee = 100 paise)
 * stored as long, never double, to avoid rounding errors.
 */
public record TickEvent(
        String symbol,
        long ltpPaise,
        long bidPaise,
        long askPaise,
        long volume,
        Instant timestamp) {
}
