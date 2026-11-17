package com.demo.watchlist.service;

/** The requested watchlist or symbol doesn't exist (or belongs to someone else). Becomes HTTP 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
