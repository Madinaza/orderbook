package com.aghaz.orderbook.trading.app;

import com.aghaz.orderbook.trading.infra.entity.LimitOrderEntity;
import com.aghaz.orderbook.trading.infra.entity.OrderAuditLogEntity;
import com.aghaz.orderbook.trading.infra.repo.OrderAuditLogRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class OrderAuditService {

    private final OrderAuditLogRepo orderAuditLogRepo;

    public OrderAuditService(OrderAuditLogRepo orderAuditLogRepo) {
        this.orderAuditLogRepo = orderAuditLogRepo;
    }

    @Transactional
    public void recordPlaced(LimitOrderEntity order) {
        record(order, "PLACED", buildPlacedMessage(order));
    }

    @Transactional
    public void recordReplaced(LimitOrderEntity order, BigDecimal oldPrice, long oldQty) {
        String message = "Order replaced from price=%s qty=%d to price=%s qty=%d"
                .formatted(formatPrice(oldPrice), oldQty, formatPrice(order.getLimitPrice()), order.getOriginalQty());
        record(order, "REPLACED", message);
    }

    @Transactional
    public void recordCancelled(LimitOrderEntity order) {
        record(order, "CANCELLED", "Order cancelled by trader.");
    }

    @Transactional
    public void recordFill(LimitOrderEntity order, long fillQty) {
        String eventType = order.getStatus().name();
        String message = order.getStatus().name().equals("FILLED")
                ? "Order fully filled. Executed quantity=%d".formatted(fillQty)
                : "Order partially filled. Executed quantity=%d, remaining quantity=%d"
                .formatted(fillQty, order.getOpenQty());

        record(order, eventType, message);
    }

    private void record(LimitOrderEntity order, String eventType, String message) {
        orderAuditLogRepo.save(new OrderAuditLogEntity(
                order.getId(),
                order.getTraderId(),
                eventType,
                message,
                Instant.now()
        ));
    }

    private String buildPlacedMessage(LimitOrderEntity order) {
        return "Order placed: side=%s, type=%s, instrument=%s, price=%s, quantity=%d"
                .formatted(
                        order.getSide().name(),
                        order.getOrderType().name(),
                        order.getInstrument(),
                        formatPrice(order.getLimitPrice()),
                        order.getOriginalQty()
                );
    }

    private String formatPrice(BigDecimal value) {
        return value == null ? "MARKET" : value.toPlainString();
    }
}