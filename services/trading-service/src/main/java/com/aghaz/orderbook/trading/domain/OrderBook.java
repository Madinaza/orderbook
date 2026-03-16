package com.aghaz.orderbook.trading.domain;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public final class OrderBook {

    private final String instrument;

    private final NavigableMap<BigDecimal, Deque<BookOrder>> bids =
            new TreeMap<>(Comparator.reverseOrder());

    private final NavigableMap<BigDecimal, Deque<BookOrder>> asks =
            new TreeMap<>();

    public OrderBook(String instrument) {
        this.instrument = Objects.requireNonNull(instrument, "instrument must not be null");
    }

    public String instrument() {
        return instrument;
    }

    public void addResting(BookOrder order) {
        if (!instrument.equals(order.instrument())) {
            throw new IllegalArgumentException("Instrument mismatch.");
        }
        if (!order.isLive()) {
            return;
        }
        if (!(order.priceIntent() instanceof PriceIntent.Limit limit)) {
            throw new IllegalArgumentException("Only LIMIT orders can rest in the book.");
        }

        NavigableMap<BigDecimal, Deque<BookOrder>> sideBook =
                order.side() == Side.BUY ? bids : asks;

        sideBook.computeIfAbsent(limit.limitPrice(), ignored -> new ArrayDeque<>())
                .addLast(order);
    }

    public Optional<BookOrder> peekBest(Side side) {
        NavigableMap<BigDecimal, Deque<BookOrder>> sideBook =
                side == Side.BUY ? bids : asks;

        while (!sideBook.isEmpty()) {
            Map.Entry<BigDecimal, Deque<BookOrder>> bestLevel = sideBook.firstEntry();
            Deque<BookOrder> queue = bestLevel.getValue();

            while (!queue.isEmpty() && !queue.peekFirst().isLive()) {
                queue.removeFirst();
            }

            if (!queue.isEmpty()) {
                return Optional.of(queue.peekFirst());
            }

            sideBook.remove(bestLevel.getKey());
        }

        return Optional.empty();
    }

    /**
     * Removes exactly one head element from the current best level.
     * This is used after the matching engine has already filled that head order.
     *
     * Important:
     * - do NOT call peekBest() here
     * - do NOT remove dead heads in a loop and then remove again
     * - remove exactly one current head item
     */
    public void removeBestHead(Side side) {
        NavigableMap<BigDecimal, Deque<BookOrder>> sideBook =
                side == Side.BUY ? bids : asks;

        while (!sideBook.isEmpty()) {
            Map.Entry<BigDecimal, Deque<BookOrder>> bestLevel = sideBook.firstEntry();
            Deque<BookOrder> queue = bestLevel.getValue();

            if (queue.isEmpty()) {
                sideBook.remove(bestLevel.getKey());
                continue;
            }

            // Remove exactly the head order that was just matched.
            queue.removeFirst();

            // Cleanup any dead heads left behind.
            while (!queue.isEmpty() && !queue.peekFirst().isLive()) {
                queue.removeFirst();
            }

            if (queue.isEmpty()) {
                sideBook.remove(bestLevel.getKey());
            }

            return;
        }

        throw new NoSuchElementException("No resting order available.");
    }
}