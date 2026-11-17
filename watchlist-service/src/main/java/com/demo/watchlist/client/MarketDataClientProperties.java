package com.demo.watchlist.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Where market-data-service is and how long to wait for it. Bound from "clients.market-data".
 * Short timeouts matter: a slow dependency must not make this service slow too.
 */
@ConfigurationProperties(prefix = "clients.market-data")
public record MarketDataClientProperties(
        String baseUrl,
        @DefaultValue("1s") Duration connectTimeout,
        @DefaultValue("2s") Duration readTimeout) {
}
