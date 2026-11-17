package com.demo.watchlist.client;

import java.math.BigDecimal;

/**
 * The fields we need from market-data-service's price response.
 * Other fields in the JSON are ignored, so market-data can add fields without breaking us.
 */
public record PriceView(
        String symbol,
        BigDecimal ltp,
        BigDecimal change,
        BigDecimal changePercent) {
}
