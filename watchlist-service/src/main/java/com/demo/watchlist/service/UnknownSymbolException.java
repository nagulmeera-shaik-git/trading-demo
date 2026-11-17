package com.demo.watchlist.service;

/** The symbol isn't a tradable instrument according to market-data-service. Becomes HTTP 400. */
public class UnknownSymbolException extends RuntimeException {

    public UnknownSymbolException(String symbol) {
        super("Unknown symbol: " + symbol);
    }
}
