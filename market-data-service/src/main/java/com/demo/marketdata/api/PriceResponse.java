package com.demo.marketdata.api;

import com.demo.marketdata.domain.PriceSnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * What the REST API returns. Internally prices are paise (long); for clients we show rupees
 * with 2 decimals, which is easier to read.
 */
public record PriceResponse(
        String symbol,
        BigDecimal ltp,
        BigDecimal bid,
        BigDecimal ask,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal change,
        BigDecimal changePercent,
        long volume,
        Instant timestamp) {

    public static PriceResponse from(PriceSnapshot s) {
        long changePaise = s.ltp() - s.open();
        BigDecimal changePercent = s.open() == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(changePaise)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(s.open()), 2, RoundingMode.HALF_UP);

        return new PriceResponse(
                s.symbol(),
                rupees(s.ltp()), rupees(s.bid()), rupees(s.ask()),
                rupees(s.open()), rupees(s.high()), rupees(s.low()),
                rupees(changePaise), changePercent,
                s.volume(), s.timestamp());
    }

    private static BigDecimal rupees(long paise) {
        return BigDecimal.valueOf(paise, 2);   // 150250 paise -> 1502.50
    }
}
