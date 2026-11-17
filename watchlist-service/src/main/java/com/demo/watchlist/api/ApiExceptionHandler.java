package com.demo.watchlist.api;

import com.demo.watchlist.client.MarketDataUnavailableException;
import com.demo.watchlist.domain.WatchlistRuleException;
import com.demo.watchlist.service.NotFoundException;
import com.demo.watchlist.service.UnknownSymbolException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * One place that turns exceptions into HTTP responses.
 * Extending ResponseEntityExceptionHandler also gives clean 400 responses for invalid
 * request bodies (@Valid failures, malformed JSON) for free.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Not found", ex.getMessage());
    }

    @ExceptionHandler(WatchlistRuleException.class)
    public ProblemDetail ruleBroken(WatchlistRuleException ex) {
        return problem(HttpStatus.CONFLICT, "Watchlist rule", ex.getMessage());
    }

    @ExceptionHandler(UnknownSymbolException.class)
    public ProblemDetail unknownSymbol(UnknownSymbolException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Unknown symbol", ex.getMessage());
    }

    @ExceptionHandler(MarketDataUnavailableException.class)
    public ProblemDetail marketDataDown(MarketDataUnavailableException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Market data unavailable",
                "Prices can't be checked right now, please try again shortly");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
