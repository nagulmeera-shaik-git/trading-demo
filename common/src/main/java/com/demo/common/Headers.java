package com.demo.common;

/** HTTP and Kafka header names shared across services. */
public final class Headers {
    /** Correlation ID created by the gateway and passed through every hop. */
    public static final String REQUEST_ID = "X-Request-Id";
    /** Authenticated user ID injected by the gateway after JWT validation (Phase 3). */
    public static final String USER_ID = "X-User-Id";
    /** Client-supplied key that makes order placement safe to retry. */
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private Headers() {}
}
