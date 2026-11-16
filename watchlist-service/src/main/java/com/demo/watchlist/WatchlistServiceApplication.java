package com.demo.watchlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** User watchlists, enriched with latest prices. */
@SpringBootApplication
public class WatchlistServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WatchlistServiceApplication.class, args);
    }
}
