package com.demo.watchlist.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Body for adding a symbol: {"symbol": "INFY"} */
public record AddSymbolRequest(
        @NotBlank(message = "symbol is required")
        @Pattern(regexp = "[A-Za-z0-9&-]{1,32}", message = "symbol must be 1-32 letters, digits, & or -")
        String symbol) {
}
