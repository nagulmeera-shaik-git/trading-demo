package com.demo.watchlist.api;

import com.demo.watchlist.domain.Watchlist;

/** A watchlist without prices, used in lists and after create/rename. */
public record WatchlistSummary(Long id, String name, int position, int symbolCount) {

    public static WatchlistSummary from(Watchlist w) {
        return new WatchlistSummary(w.getId(), w.getName(), w.getPosition(), w.getItems().size());
    }
}
