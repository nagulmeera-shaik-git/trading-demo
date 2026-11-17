package com.demo.marketdata.domain;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store of the latest price for each symbol.
 * Written by the price feed thread, read by HTTP request threads, so it uses a ConcurrentHashMap
 * and stores immutable records.
 */
@Component
public class PriceStore {

    private final Map<String, PriceSnapshot> latest = new ConcurrentHashMap<>();

    public void update(PriceSnapshot snapshot) {
        latest.put(snapshot.symbol(), snapshot);
    }

    public Optional<PriceSnapshot> find(String symbol) {
        return Optional.ofNullable(latest.get(symbol.toUpperCase(Locale.ROOT)));
    }

    public List<PriceSnapshot> all() {
        return latest.values().stream()
                .sorted(Comparator.comparing(PriceSnapshot::symbol))
                .toList();
    }
}
