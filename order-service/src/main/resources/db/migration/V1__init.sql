-- Paper orders. Prices are stored in paise as BIGINT.
CREATE TABLE orders (
    id                UUID PRIMARY KEY,
    user_id           BIGINT      NOT NULL,
    symbol            VARCHAR(32) NOT NULL,
    side              VARCHAR(4)  NOT NULL,
    order_type        VARCHAR(8)  NOT NULL,
    quantity          INT         NOT NULL CHECK (quantity > 0),
    filled_qty        INT         NOT NULL DEFAULT 0,
    limit_price       BIGINT,
    trigger_price     BIGINT,
    avg_fill_price    BIGINT,
    status            VARCHAR(20) NOT NULL,
    idempotency_key   VARCHAR(64) NOT NULL,
    version           INT         NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, idempotency_key)
);

CREATE INDEX idx_orders_user_created ON orders (user_id, created_at DESC);
CREATE INDEX idx_orders_open_by_symbol ON orders (symbol) WHERE status IN ('OPEN', 'TRIGGER_PENDING');
