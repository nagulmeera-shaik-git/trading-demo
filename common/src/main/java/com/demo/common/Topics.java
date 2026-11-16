package com.demo.common;

/** Kafka topic names shared by all services. Keep them in one place so producers and consumers never drift apart. */
public final class Topics {
    /** Live price ticks. Key = symbol, so all ticks for one share stay in order. */
    public static final String MARKET_TICKS = "market.ticks";
    /** New paper orders accepted by order-service. Key = symbol. */
    public static final String ORDERS_PLACED = "orders.placed";
    /** Fills produced by the fill engine. Key = userId, so one user's updates stay in order. */
    public static final String ORDERS_FILLED = "orders.filled";

    private Topics() {}
}
