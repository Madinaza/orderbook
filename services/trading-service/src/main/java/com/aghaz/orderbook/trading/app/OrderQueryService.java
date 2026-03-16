package com.aghaz.orderbook.trading.app;

import com.aghaz.orderbook.shared_contracts.api.PagedResponse;
import com.aghaz.orderbook.shared_contracts.exceptions.NotFoundException;
import com.aghaz.orderbook.trading.domain.OrderStatus;
import com.aghaz.orderbook.trading.domain.OrderType;
import com.aghaz.orderbook.trading.domain.Side;
import com.aghaz.orderbook.trading.dto.OrderResponse;
import com.aghaz.orderbook.trading.infra.entity.LimitOrderEntity;
import com.aghaz.orderbook.trading.infra.repo.LimitOrderRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class OrderQueryService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "status", "instrument"
    );

    private final LimitOrderRepo limitOrderRepo;

    public OrderQueryService(LimitOrderRepo limitOrderRepo) {
        this.limitOrderRepo = limitOrderRepo;
    }

    public List<OrderResponse> myOrders(long traderId) {
        return myOrders(traderId, null, null, null, null, 0, 50, "createdAt", "desc").content();
    }

    public PagedResponse<OrderResponse> myOrders(long traderId,
                                                 String instrument,
                                                 String status,
                                                 String side,
                                                 String orderType,
                                                 int page,
                                                 int size,
                                                 String sortBy,
                                                 String sortDirection) {
        String normalizedInstrument = normalizeOptionalInstrument(instrument);
        OrderStatus normalizedStatus = normalizeOptionalStatus(status);
        Side normalizedSide = normalizeOptionalSide(side);
        OrderType normalizedOrderType = normalizeOptionalOrderType(orderType);

        int safePage = Math.max(page, 0);
        int safeSize = normalizePageSize(size);
        String safeSortBy = normalizeOrderSortBy(sortBy);
        Sort.Direction safeDirection = normalizeSortDirection(sortDirection);

        var pageable = PageRequest.of(safePage, safeSize, Sort.by(safeDirection, safeSortBy));
        var pageResult = limitOrderRepo.findAllByTraderId(traderId, pageable);

        var filteredContent = pageResult.getContent()
                .stream()
                .filter(order -> matchesInstrument(order, normalizedInstrument))
                .filter(order -> matchesStatus(order, normalizedStatus))
                .filter(order -> matchesSide(order, normalizedSide))
                .filter(order -> matchesOrderType(order, normalizedOrderType))
                .map(this::toResponse)
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

    public OrderResponse byId(long traderId, long orderId) {
        LimitOrderEntity entity = limitOrderRepo.findByIdAndTraderId(orderId, traderId)
                .orElseThrow(() -> new NotFoundException("Order not found."));
        return toResponse(entity);
    }

    private boolean matchesInstrument(LimitOrderEntity order, String instrument) {
        return instrument == null || instrument.equals(order.getInstrument());
    }

    private boolean matchesStatus(LimitOrderEntity order, OrderStatus status) {
        return status == null || status == order.getStatus();
    }

    private boolean matchesSide(LimitOrderEntity order, Side side) {
        return side == null || side == order.getSide();
    }

    private boolean matchesOrderType(LimitOrderEntity order, OrderType orderType) {
        return orderType == null || orderType == order.getOrderType();
    }

    private int normalizePageSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private String normalizeOrderSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }
        String normalized = sortBy.trim();
        if (!ALLOWED_SORT_FIELDS.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported order sort field: " + sortBy);
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

    private String normalizeOptionalInstrument(String instrument) {
        if (instrument == null || instrument.isBlank()) {
            return null;
        }
        return instrument.trim().toUpperCase(Locale.ROOT);
    }

    private OrderStatus normalizeOptionalStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Status must be one of: NEW, PARTIALLY_FILLED, FILLED, CANCELLED, REJECTED."
            );
        }
    }

    private Side normalizeOptionalSide(String side) {
        if (side == null || side.isBlank()) {
            return null;
        }

        try {
            return Side.valueOf(side.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Side must be BUY or SELL.");
        }
    }

    private OrderType normalizeOptionalOrderType(String orderType) {
        if (orderType == null || orderType.isBlank()) {
            return null;
        }

        try {
            return OrderType.valueOf(orderType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Order type must be LIMIT or MARKET.");
        }
    }

    private OrderResponse toResponse(LimitOrderEntity e) {
        return new OrderResponse(
                e.getId(),
                e.getClientOrderId(),
                e.getTraderId(),
                e.getInstrument(),
                e.getSide().name(),
                e.getOrderType().name(),
                e.getLimitPrice(),
                e.getOriginalQty(),
                e.getOpenQty(),
                e.getStatus().name(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}