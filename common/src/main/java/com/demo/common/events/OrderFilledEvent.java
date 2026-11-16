package com.demo.common.events;

import com.demo.common.model.Side;

import java.time.Instant;
import java.util.UUID;

/**
 * Published by the fill engine when a paper order executes.
 * Consumers must be idempotent: store eventId and skip duplicates.
 */
public record OrderFilledEvent(
        UUID eventId,
        UUID orderId,
        long userId,
        String symbol,
        Side side,
        int quantity,
        long fillPricePaise,
        Instant timestamp) {
}
