package com.demo.watchlist.repository;

import com.demo.watchlist.domain.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data writes the SQL for these methods from their names. For example
 * findByIdAndUserId -> SELECT ... FROM watchlists WHERE id = ? AND user_id = ?
 */
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUserIdOrderByPosition(long userId);

    /** Always look up by id AND owner, so one user can never read another user's watchlist. */
    Optional<Watchlist> findByIdAndUserId(long id, long userId);

    long countByUserId(long userId);
}
