package com.aghaz.orderbook.trading.app;

import com.aghaz.orderbook.shared_contracts.api.PagedResponse;
import com.aghaz.orderbook.trading.dto.TradeResponse;
import com.aghaz.orderbook.trading.infra.entity.TradeFillEntity;
import com.aghaz.orderbook.trading.infra.repo.TradeFillRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class TradeQueryService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "executedAt", "price", "quantity", "instrument"
    );

    private final TradeFillRepo tradeFillRepo;

    public TradeQueryService(TradeFillRepo tradeFillRepo) {
        this.tradeFillRepo = tradeFillRepo;
    }

    public List<TradeResponse> myTrades(long traderId) {
        return myTrades(traderId, null, null, null, null, 0, 50, "executedAt", "desc").content();
    }

    public PagedResponse<TradeResponse> myTrades(long traderId,
                                                 String instrument,
                                                 LocalDate from,
                                                 LocalDate to,
                                                 String side,
                                                 int page,
                                                 int size,
                                                 String sortBy,
                                                 String sortDirection) {
        String normalizedInstrument = normalizeOptionalInstrument(instrument);
        String normalizedSide = normalizeOptionalSide(side);

        int safePage = Math.max(page, 0);
        int safeSize = normalizePageSize(size);
        String safeSortBy = normalizeTradeSortBy(sortBy);
        Sort.Direction safeDirection = normalizeSortDirection(sortDirection);

        var pageable = PageRequest.of(safePage, safeSize, Sort.by(safeDirection, safeSortBy));
        var pageResult = tradeFillRepo.findByBuyTraderIdOrSellTraderId(traderId, traderId, pageable);

        var filteredContent = pageResult.getContent()
                .stream()
                .filter(t -> matchesInstrument(t, normalizedInstrument))
                .filter(t -> matchesFrom(t, from))
                .filter(t -> matchesTo(t, to))
                .filter(t -> matchesSide(t, traderId, normalizedSide))
                .map(t -> toResponseForTrader(t, traderId))
                .toList();

        return new PagedResponse<>(
                filteredContent,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isFirst(),
                pageResult.isLast(),
                safeSortBy,
                safeDirection.name().toLowerCase(Locale.ROOT)
        );
    }

    public PagedResponse<TradeResponse> byInstrument(String instrument,
                                                     int page,
                                                     int size,
                                                     String sortBy,
                                                     String sortDirection) {
        String normalized = normalizeRequiredInstrument(instrument);
        int safePage = Math.max(page, 0);
        int safeSize = normalizePageSize(size);
        String safeSortBy = normalizeTradeSortBy(sortBy);
        Sort.Direction safeDirection = normalizeSortDirection(sortDirection);

        var pageable = PageRequest.of(safePage, safeSize, Sort.by(safeDirection, safeSortBy));
        var pageResult = tradeFillRepo.findByInstrument(normalized, pageable);

        var content = pageResult.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PagedResponse<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isFirst(),
                pageResult.isLast(),
                safeSortBy,
                safeDirection.name().toLowerCase(Locale.ROOT)
        );
    }

    private int normalizePageSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private String normalizeTradeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "executedAt";
        }
        String normalized = sortBy.trim();
        if (!ALLOWED_SORT_FIELDS.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported trade sort field: " + sortBy);
        }
        return normalized;
    }

    private Sort.Direction normalizeSortDirection(String sortDirection) {
        if (sortDirection == null || sortDirection.isBlank()) {
            return Sort.Direction.DESC;
        }
        try {
            return Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Sort direction must be asc or desc.");
        }
    }

    private boolean matchesInstrument(TradeFillEntity trade, String instrument) {
        return instrument == null || instrument.equalsIgnoreCase(trade.getInstrument());
    }

    private boolean matchesFrom(TradeFillEntity trade, LocalDate from) {
        if (from == null) {
            return true;
        }
        return !trade.getExecutedAt().isBefore(from.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    private boolean matchesTo(TradeFillEntity trade, LocalDate to) {
        if (to == null) {
            return true;
        }
        return trade.getExecutedAt().isBefore(to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    private boolean matchesSide(TradeFillEntity trade, long traderId, String side) {
        if (side == null) {
            return true;
        }

        if (side.equals("BUY")) {
            return trade.getBuyTraderId() == traderId;
        }

        if (side.equals("SELL")) {
            return trade.getSellTraderId() == traderId;
        }

        return true;
    }

    private String normalizeRequiredInstrument(String instrument) {
        if (instrument == null || instrument.isBlank()) {
            throw new IllegalArgumentException("Instrument is required.");
        }
        return instrument.trim().toUpperCase();
    }

    private String normalizeOptionalInstrument(String instrument) {
        if (instrument == null || instrument.isBlank()) {
            return null;
        }
        return instrument.trim().toUpperCase();
    }

    private String normalizeOptionalSide(String side) {
        if (side == null || side.isBlank()) {
            return null;
        }

        String normalized = side.trim().toUpperCase();
        if (normalized.equals("ALL")) {
            return null;
        }

        if (!normalized.equals("BUY") && !normalized.equals("SELL")) {
            throw new IllegalArgumentException("Side must be BUY, SELL, or empty.");
        }

        return normalized;
    }

    private TradeResponse toResponse(TradeFillEntity e) {
        return new TradeResponse(
                e.getId(),
                e.getInstrument(),
                e.getBuyOrderId(),
                e.getSellOrderId(),
                e.getBuyTraderId(),
                e.getSellTraderId(),
                e.getPrice(),
                e.getQuantity(),
                e.getExecutedAt(),
                null
        );
    }

    private TradeResponse toResponseForTrader(TradeFillEntity e, long traderId) {
        String mySide = e.getBuyTraderId() == traderId ? "BUY" : "SELL";

        return new TradeResponse(
                e.getId(),
                e.getInstrument(),
                e.getBuyOrderId(),
                e.getSellOrderId(),
                e.getBuyTraderId(),
                e.getSellTraderId(),
                e.getPrice(),
                e.getQuantity(),
                e.getExecutedAt(),
                mySide
        );
    }
}