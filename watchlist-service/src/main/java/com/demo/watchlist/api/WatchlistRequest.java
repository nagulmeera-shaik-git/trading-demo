package com.demo.watchlist.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body for creating or renaming a watchlist: {"name": "Banks"} */
public record WatchlistRequest(
        @NotBlank(message = "name is required")
        @Size(max = 40, message = "name must be at most 40 characters")
        String name) {
}
