package com.demo.watchlist.api;

import java.util.List;

/**
 * A watchlist with live prices.
 * pricesAvailable = false means market-data-service couldn't be reached; the symbols are still shown.
 */
public record WatchlistDetail(
        Long id,
        String name,
        int position,
        boolean pricesAvailable,
        List<WatchlistItemView> items) {
}
