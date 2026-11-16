-- A user can have several watchlists (tabs), each with an ordered list of symbols.
CREATE TABLE watchlists (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    name       VARCHAR(40) NOT NULL,
    position   SMALLINT    NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, position)
);

CREATE TABLE watchlist_items (
    watchlist_id BIGINT      NOT NULL REFERENCES watchlists (id) ON DELETE CASCADE,
    symbol       VARCHAR(32) NOT NULL,
    sort_order   SMALLINT    NOT NULL,
    PRIMARY KEY (watchlist_id, symbol)
);
