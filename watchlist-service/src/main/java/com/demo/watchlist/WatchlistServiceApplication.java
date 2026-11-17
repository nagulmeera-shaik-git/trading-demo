package com.demo.watchlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** User watchlists, enriched with latest prices. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class WatchlistServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WatchlistServiceApplication.class, args);
    }
}
