-- Demo money per user, in paise.
CREATE TABLE funds (
    user_id   BIGINT PRIMARY KEY,
    available BIGINT NOT NULL,
    blocked   BIGINT NOT NULL DEFAULT 0,
    version   INT    NOT NULL DEFAULT 0
);

-- Shares held per user.
CREATE TABLE holdings (
    user_id   BIGINT      NOT NULL,
    symbol    VARCHAR(32) NOT NULL,
    quantity  INT         NOT NULL,
    avg_price BIGINT      NOT NULL,
    PRIMARY KEY (user_id, symbol)
);

-- Event IDs already applied, so a redelivered Kafka message is not applied twice.
CREATE TABLE processed_events (
    event_id     UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
