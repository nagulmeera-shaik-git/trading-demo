package com.demo.watchlist.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One watchlist (a tab in the app) belonging to one user.
 * Maps to the "watchlists" table; its symbols live in "watchlist_items".
 *
 * Business rules live here, on the entity itself, so they can't be bypassed.
 */
@Entity
@Table(name = "watchlists")
public class Watchlist {

    public static final int MAX_SYMBOLS = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // BIGSERIAL in PostgreSQL
    private Long id;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false)
    private short position;    // tab number 1..7 (SMALLINT in the database)

    // created_at is filled by the database (DEFAULT now()), so it isn't mapped here

    // The symbols are simple values owned by this watchlist, so they are an element collection,
    // not a separate entity. Loaded lazily, ordered by sort_order.
    @ElementCollection
    @CollectionTable(name = "watchlist_items", joinColumns = @JoinColumn(name = "watchlist_id"))
    @OrderBy("sortOrder")
    private List<WatchlistItem> items = new ArrayList<>();

    protected Watchlist() {
        // required by JPA
    }

    public Watchlist(long userId, String name, short position) {
        this.userId = userId;
        this.name = name;
        this.position = position;
    }

    public void rename(String newName) {
        this.name = newName;
    }

    public boolean contains(String symbol) {
        return items.stream().anyMatch(i -> i.getSymbol().equals(symbol));
    }

    public void addSymbol(String symbol) {
        if (contains(symbol)) {
            throw new WatchlistRuleException(symbol + " is already in watchlist '" + name + "'");
        }
        if (items.size() >= MAX_SYMBOLS) {
            throw new WatchlistRuleException("A watchlist can hold at most " + MAX_SYMBOLS + " symbols");
        }
        short nextOrder = (short) (items.stream().mapToInt(WatchlistItem::getSortOrder).max().orElse(0) + 1);
        items.add(new WatchlistItem(symbol, nextOrder));
    }

    /** @return true if the symbol was there and has been removed */
    public boolean removeSymbol(String symbol) {
        return items.removeIf(i -> i.getSymbol().equals(symbol));
    }

    public Long getId() { return id; }
    public long getUserId() { return userId; }
    public String getName() { return name; }
    public short getPosition() { return position; }
    public List<WatchlistItem> getItems() { return Collections.unmodifiableList(items); }
}
