package com.demo.watchlist.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Plain unit test of the entity's business rules. No Spring, no database. */
class WatchlistTest {

    @Test
    void addsSymbolsInOrder() {
        Watchlist w = new Watchlist(1, "IT", (short) 1);
        w.addSymbol("INFY");
        w.addSymbol("TCS");

        assertThat(w.getItems()).extracting(WatchlistItem::getSymbol).containsExactly("INFY", "TCS");
        assertThat(w.getItems()).extracting(WatchlistItem::getSortOrder).containsExactly((short) 1, (short) 2);
    }

    @Test
    void rejectsDuplicates() {
        Watchlist w = new Watchlist(1, "IT", (short) 1);
        w.addSymbol("INFY");

        assertThatThrownBy(() -> w.addSymbol("INFY")).isInstanceOf(WatchlistRuleException.class);
    }

    @Test
    void rejectsMoreThanFiftySymbols() {
        Watchlist w = new Watchlist(1, "Big", (short) 1);
        for (int i = 0; i < Watchlist.MAX_SYMBOLS; i++) {
            w.addSymbol("S" + i);
        }

        assertThatThrownBy(() -> w.addSymbol("ONEMORE")).isInstanceOf(WatchlistRuleException.class);
    }

    @Test
    void removesSymbol() {
        Watchlist w = new Watchlist(1, "IT", (short) 1);
        w.addSymbol("INFY");

        assertThat(w.removeSymbol("INFY")).isTrue();
        assertThat(w.removeSymbol("INFY")).isFalse();
        assertThat(w.getItems()).isEmpty();
    }
}
