package com.demo.watchlist.api;

import com.demo.watchlist.client.PriceView;

import java.math.BigDecimal;

/** One row of a watchlist screen. Price fields are null when no price is available. */
public record WatchlistItemView(
        String symbol,
        BigDecimal ltp,
        BigDecimal change,
        BigDecimal changePercent) {

    public static WatchlistItemView of(String symbol, PriceView price) {
        return price == null
                ? new WatchlistItemView(symbol, null, null, null)
                : new WatchlistItemView(symbol, price.ltp(), price.change(), price.changePercent());
    }
}
