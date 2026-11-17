package com.demo.watchlist.api;

import com.demo.common.Headers;
import com.demo.watchlist.service.WatchlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * REST API for watchlists. Through the gateway: /api/watchlists/...
 *
 * Who is the user? From Phase 3 the gateway checks the JWT and sends the user id in the
 * X-User-Id header. Until then, the header defaults to user 1 so you can test easily.
 */
@RestController
@RequestMapping("/watchlists")
public class WatchlistController {

    private final WatchlistService service;

    public WatchlistController(WatchlistService service) {
        this.service = service;
    }

    @GetMapping
    public List<WatchlistSummary> list(@RequestHeader(name = Headers.USER_ID, defaultValue = "1") long userId) {
        return service.list(userId);
    }

    @PostMapping
    public ResponseEntity<WatchlistSummary> create(
            @RequestHeader(name = Headers.USER_ID, defaultValue = "1") long userId,
            @Valid @RequestBody WatchlistRequest request) {
        WatchlistSummary created = service.create(userId, request.name());
        return ResponseEntity.created(URI.create("/watchlists/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public WatchlistDetail get(
            @RequestHeader(name = Headers.USER_ID, defaultValue = "1") long userId,
            @PathVariable long id) {
        return service.get(userId, id);
    }

    @PutMapping("/{id}")
    public WatchlistSummary rename(
            @RequestHeader(name = Headers.USER_ID, defaultValue = "1") long userId,
            @PathVariable long id,
            @Valid @RequestBody WatchlistRequest request) {
        return service.rename(userId, id, request.name());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(name = Headers.USER_ID, defaultValue = "1") long userId,
            @PathVariable long id) {
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<WatchlistDetail> addSymbol(
            @RequestHeader(name = Headers.USER_ID, defaultValue = "1") long userId,
            @PathVariable long id,
            @Valid @RequestBody AddSymbolRequest request) {
        service.addSymbol(userId, id, request.symbol());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.get(userId, id));
    }

    @DeleteMapping("/{id}/items/{symbol}")
    public ResponseEntity<Void> removeSymbol(
            @RequestHeader(name = Headers.USER_ID, defaultValue = "1") long userId,
            @PathVariable long id,
            @PathVariable String symbol) {
        service.removeSymbol(userId, id, symbol);
        return ResponseEntity.noContent().build();
    }
}
