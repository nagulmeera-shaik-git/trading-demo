package com.demo.marketdata.api;

import com.demo.marketdata.domain.PriceStore;
import com.demo.marketdata.feed.PriceFeed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

/**
 * REST API for prices.
 *   GET /market/instruments        -> list of symbols
 *   GET /market/prices             -> latest price for every symbol
 *   GET /market/prices/{symbol}    -> latest price for one symbol
 * Through the gateway these become /api/market/...
 */
@RestController
@RequestMapping("/market")
public class PriceController {

    private final PriceStore store;
    private final PriceFeed feed;

    public PriceController(PriceStore store, PriceFeed feed) {
        this.store = store;
        this.feed = feed;
    }

    @GetMapping("/instruments")
    public List<String> instruments() {
        return feed.symbols().stream().sorted().toList();
    }

    @GetMapping("/prices")
    public List<PriceResponse> allPrices() {
        return store.all().stream().map(PriceResponse::from).toList();
    }

    @GetMapping("/prices/{symbol}")
    public PriceResponse price(@PathVariable String symbol) {
        String normalized = symbol.toUpperCase(Locale.ROOT);
        return store.find(normalized)
                .map(PriceResponse::from)
                .orElseThrow(() -> new UnknownSymbolException(normalized));
    }
}
