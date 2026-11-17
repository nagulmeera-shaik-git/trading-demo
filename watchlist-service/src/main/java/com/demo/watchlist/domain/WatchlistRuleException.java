package com.demo.watchlist.domain;

/** A business rule was broken, e.g. too many watchlists or a duplicate symbol. Becomes HTTP 409. */
public class WatchlistRuleException extends RuntimeException {

    public WatchlistRuleException(String message) {
        super(message);
    }
}
