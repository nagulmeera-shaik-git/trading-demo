package com.demo.marketdata.api;

/** Thrown when a client asks for a symbol we don't have a price for. */
public class UnknownSymbolException extends RuntimeException {

    public UnknownSymbolException(String symbol) {
        super("No price available for symbol " + symbol);
    }
}
