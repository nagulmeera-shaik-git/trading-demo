package com.demo.watchlist.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** One symbol inside a watchlist. A row in "watchlist_items". */
@Embeddable
public class WatchlistItem {

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder;

    protected WatchlistItem() {
        // required by JPA
    }

    public WatchlistItem(String symbol, short sortOrder) {
        this.symbol = symbol;
        this.sortOrder = sortOrder;
    }

    public String getSymbol() { return symbol; }
    public short getSortOrder() { return sortOrder; }
}
