package com.aghaz.orderbook.trading.sor;

import com.aghaz.orderbook.trading.domain.Side;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Selects the best venue based on side and fee-adjusted economics.
 *
 * BUY:
 *   lowest effective total cost wins
 *
 * SELL:
 *   highest effective net proceeds wins
 */
public final class SmartOrderRouter {

    private final FeeLadder feeLadder;

    public SmartOrderRouter() {
        this(new FeeLadder());
    }

    public SmartOrderRouter(FeeLadder feeLadder) {
        this.feeLadder = Objects.requireNonNull(feeLadder, "feeLadder must not be null");
    }

    public Optional<ExchangeQuote> bestVenue(Side side, List<ExchangeQuote> quotes) {
        return bestVenue(side, quotes, 1);
    }

    public Optional<ExchangeQuote> bestVenue(Side side, List<ExchangeQuote> quotes, long quantity) {
        Objects.requireNonNull(side, "side must not be null");
        Objects.requireNonNull(quotes, "quotes must not be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }

        List<ExchangeQuote> eligibleQuotes = quotes.stream()
                .filter(q -> q.availableQuantity() >= quantity)
                .toList();

        if (eligibleQuotes.isEmpty()) {
            return Optional.empty();
        }

        Comparator<ExchangeQuote> comparator = switch (side) {
            case BUY -> Comparator
                    .comparing((ExchangeQuote q) -> effectiveBuyCost(q, quantity))
                    .thenComparing(ExchangeQuote::exchangeCode);

            case SELL -> Comparator
                    .comparing((ExchangeQuote q) -> effectiveSellProceeds(q, quantity))
                    .reversed()
                    .thenComparing(ExchangeQuote::exchangeCode);
        };

        return eligibleQuotes.stream().min(comparator);
    }

    public BigDecimal effectiveBuyCost(ExchangeQuote quote, long quantity) {
        return feeLadder.effectiveBuyCost(quote.price(), quantity, quote.feeRate());
    }

    public BigDecimal effectiveSellProceeds(ExchangeQuote quote, long quantity) {
        return feeLadder.effectiveSellProceeds(quote.price(), quantity, quote.feeRate());
    }
}