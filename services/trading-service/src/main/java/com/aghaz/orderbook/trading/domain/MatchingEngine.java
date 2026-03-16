package com.aghaz.orderbook.trading.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Price-time priority matching engine.
 *
 * Rules:
 * - Incoming BUY matches best resting SELLs
 * - Incoming SELL matches best resting BUYs
 * - LIMIT orders only trade when prices cross
 * - MARKET orders consume available liquidity until exhausted or book empty
 * - Resting LIMIT orders remain in the book if not fully filled
 */
public final class MatchingEngine {

    public List<TradeFill> match(OrderBook book, BookOrder incoming) {
        if (!incoming.instrument().equals(book.instrument())) {
            throw new IllegalArgumentException("Instrument mismatch.");
        }

        if (!incoming.isLive()) {
            return List.of();
        }

        Side aggressorSide = incoming.side();
        Side restingSide = aggressorSide.opposite();

        BigDecimal aggressorLimit = null;
        if (incoming.priceIntent() instanceof PriceIntent.Limit limit) {
            aggressorLimit = limit.limitPrice();
        }

        List<TradeFill> fills = new ArrayList<>();

        while (incoming.openQty() > 0) {
            Optional<BookOrder> bestRestingOpt = book.peekBest(restingSide);
            if (bestRestingOpt.isEmpty()) {
                break;
            }

            BookOrder resting = bestRestingOpt.get();
            BigDecimal restingPrice = ((PriceIntent.Limit) resting.priceIntent()).limitPrice();

            if (!crosses(aggressorSide, aggressorLimit, restingPrice)) {
                break;
            }

            long fillQty = Math.min(incoming.openQty(), resting.openQty());

            incoming.applyFill(fillQty);
            resting.applyFill(fillQty);

            boolean incomingIsBuy = aggressorSide == Side.BUY;

            long buyOrderId = incomingIsBuy ? incoming.id() : resting.id();
            long sellOrderId = incomingIsBuy ? resting.id() : incoming.id();

            long buyTraderId = incomingIsBuy ? incoming.traderId() : resting.traderId();
            long sellTraderId = incomingIsBuy ? resting.traderId() : incoming.traderId();

            fills.add(TradeFill.now(
                    incoming.instrument(),
                    buyOrderId,
                    sellOrderId,
                    buyTraderId,
                    sellTraderId,
                    restingPrice,
                    fillQty
            ));

            if (!resting.isLive()) {
                book.removeBestHead(restingSide);
            }
        }

        if (incoming.openQty() > 0 && incoming.priceIntent() instanceof PriceIntent.Limit) {
            book.addResting(incoming);
        }

        return fills;
    }

    private boolean crosses(Side aggressorSide, BigDecimal aggressorLimit, BigDecimal restingPrice) {
        if (aggressorLimit == null) {
            return true;
        }

        return aggressorSide == Side.BUY
                ? aggressorLimit.compareTo(restingPrice) >= 0
                : aggressorLimit.compareTo(restingPrice) <= 0;
    }
}