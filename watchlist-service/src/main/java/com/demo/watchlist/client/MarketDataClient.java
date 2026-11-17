package com.demo.watchlist.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Calls market-data-service over HTTP. This is the only class that knows market-data's URLs,
 * so if its API changes, only this class changes.
 */
@Component
public class MarketDataClient {

    private final RestClient rest;

    public MarketDataClient(RestClient marketDataRestClient) {
        this.rest = marketDataRestClient;
    }

    /** Latest prices for all symbols, in one call (instead of one call per symbol). */
    public Map<String, PriceView> latestPrices() {
        try {
            List<PriceView> prices = rest.get()
                    .uri("/market/prices")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PriceView>>() {});
            return prices == null
                    ? Map.of()
                    : prices.stream().collect(Collectors.toMap(PriceView::symbol, Function.identity()));
        } catch (RestClientException e) {
            throw new MarketDataUnavailableException("Could not fetch prices from market-data-service", e);
        }
    }

    /** @return true if market-data knows this symbol, false if it answered 404 */
    public boolean symbolExists(String symbol) {
        try {
            rest.get()
                    .uri("/market/prices/{symbol}", symbol)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException e) {
            throw new MarketDataUnavailableException("Could not check symbol with market-data-service", e);
        }
    }
}
