package com.demo.common.events;

import com.demo.common.model.OrderType;
import com.demo.common.model.Side;

import java.time.Instant;
import java.util.UUID;

/** Published by order-service when a paper order is accepted. */
public record OrderPlacedEvent(
        UUID eventId,
        UUID orderId,
        long userId,
        String symbol,
        Side side,
        OrderType orderType,
        int quantity,
        Long limitPricePaise,
        Long triggerPricePaise,
        Instant timestamp) {
}
