package com.demo.watchlist.service;

import com.demo.watchlist.api.WatchlistDetail;
import com.demo.watchlist.api.WatchlistItemView;
import com.demo.watchlist.api.WatchlistSummary;
import com.demo.watchlist.client.MarketDataClient;
import com.demo.watchlist.client.MarketDataUnavailableException;
import com.demo.watchlist.client.PriceView;
import com.demo.watchlist.domain.Watchlist;
import com.demo.watchlist.domain.WatchlistRuleException;
import com.demo.watchlist.repository.WatchlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Watchlist use cases. Methods are read-only transactions by default;
 * methods that change data override that with @Transactional.
 */
@Service
@Transactional(readOnly = true)
public class WatchlistService {

    private static final Logger log = LoggerFactory.getLogger(WatchlistService.class);
    static final int MAX_WATCHLISTS = 7;

    private final WatchlistRepository repository;
    private final MarketDataClient marketData;

    public WatchlistService(WatchlistRepository repository, MarketDataClient marketData) {
        this.repository = repository;
        this.marketData = marketData;
    }

    public List<WatchlistSummary> list(long userId) {
        return repository.findByUserIdOrderByPosition(userId).stream()
                .map(WatchlistSummary::from)
                .toList();
    }

    @Transactional
    public WatchlistSummary create(long userId, String name) {
        List<Watchlist> existing = repository.findByUserIdOrderByPosition(userId);
        if (existing.size() >= MAX_WATCHLISTS) {
            throw new WatchlistRuleException("You can have at most " + MAX_WATCHLISTS + " watchlists");
        }
        Watchlist saved = repository.save(new Watchlist(userId, name.trim(), firstFreePosition(existing)));
        log.info("User {} created watchlist {} '{}'", userId, saved.getId(), saved.getName());
        return WatchlistSummary.from(saved);
    }

    /** Watchlist with live prices. If market-data is down, still returns the symbols, just without prices. */
    public WatchlistDetail get(long userId, long watchlistId) {
        Watchlist watchlist = find(userId, watchlistId);

        Map<String, PriceView> prices;
        boolean pricesAvailable;
        try {
            prices = marketData.latestPrices();
            pricesAvailable = true;
        } catch (MarketDataUnavailableException e) {
            log.warn("Showing watchlist {} without prices: {}", watchlistId, e.getMessage());
            prices = Map.of();
            pricesAvailable = false;
        }

        Map<String, PriceView> finalPrices = prices;
        List<WatchlistItemView> items = watchlist.getItems().stream()
                .map(item -> WatchlistItemView.of(item.getSymbol(), finalPrices.get(item.getSymbol())))
                .toList();

        return new WatchlistDetail(watchlist.getId(), watchlist.getName(), watchlist.getPosition(),
                pricesAvailable, items);
    }

    @Transactional
    public WatchlistSummary rename(long userId, long watchlistId, String newName) {
        Watchlist watchlist = find(userId, watchlistId);
        watchlist.rename(newName.trim());
        // no save() needed: the entity is managed, so JPA writes the change when the transaction commits
        return WatchlistSummary.from(watchlist);
    }

    @Transactional
    public void delete(long userId, long watchlistId) {
        repository.delete(find(userId, watchlistId));
    }

    @Transactional
    public void addSymbol(long userId, long watchlistId, String rawSymbol) {
        String symbol = rawSymbol.trim().toUpperCase(Locale.ROOT);
        Watchlist watchlist = find(userId, watchlistId);

        // cheap local checks first, remote call last
        if (watchlist.contains(symbol)) {
            throw new WatchlistRuleException(symbol + " is already in watchlist '" + watchlist.getName() + "'");
        }
        if (!marketData.symbolExists(symbol)) {
            throw new UnknownSymbolException(symbol);
        }
        watchlist.addSymbol(symbol);
    }

    @Transactional
    public void removeSymbol(long userId, long watchlistId, String rawSymbol) {
        String symbol = rawSymbol.trim().toUpperCase(Locale.ROOT);
        Watchlist watchlist = find(userId, watchlistId);
        if (!watchlist.removeSymbol(symbol)) {
            throw new NotFoundException(symbol + " is not in watchlist " + watchlistId);
        }
    }

    private Watchlist find(long userId, long watchlistId) {
        // a watchlist owned by someone else looks exactly like one that doesn't exist (404, not 403),
        // so users can't discover other people's watchlist ids
        return repository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new NotFoundException("Watchlist " + watchlistId + " not found"));
    }

    private static short firstFreePosition(List<Watchlist> existing) {
        Set<Short> used = existing.stream().map(Watchlist::getPosition).collect(Collectors.toSet());
        for (short p = 1; p <= MAX_WATCHLISTS; p++) {
            if (!used.contains(p)) {
                return p;
            }
        }
        throw new WatchlistRuleException("No free watchlist position");
    }
}
