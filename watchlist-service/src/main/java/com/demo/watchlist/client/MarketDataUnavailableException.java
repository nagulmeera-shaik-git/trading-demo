package com.demo.watchlist.client;

/** market-data-service is down, slow, or returned an unexpected error. */
public class MarketDataUnavailableException extends RuntimeException {

    public MarketDataUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
