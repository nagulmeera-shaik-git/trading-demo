package com.demo.marketdata.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Settings for the mock price feed, bound from "market.mock" in application.yml.
 *
 * @param tickIntervalMs how often new prices are generated, in milliseconds
 * @param instruments    symbol -> starting price in rupees
 */
@ConfigurationProperties(prefix = "market.mock")
public record MockFeedProperties(
        @DefaultValue("500") long tickIntervalMs,
        Map<String, BigDecimal> instruments) {

    public MockFeedProperties {
        // sorted, unmodifiable copy: stable order and nobody can change it later
        instruments = instruments == null ? Map.of() : Collections.unmodifiableMap(new TreeMap<>(instruments));
    }
}
