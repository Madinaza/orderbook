package com.aghaz.orderbook.trading.app;

import com.aghaz.orderbook.shared_contracts.exceptions.NotFoundException;
import com.aghaz.orderbook.trading.dto.OrderAuditEventResponse;
import com.aghaz.orderbook.trading.infra.repo.LimitOrderRepo;
import com.aghaz.orderbook.trading.infra.repo.OrderAuditLogRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderAuditQueryService {

    private final LimitOrderRepo limitOrderRepo;
    private final OrderAuditLogRepo orderAuditLogRepo;

    public OrderAuditQueryService(LimitOrderRepo limitOrderRepo, OrderAuditLogRepo orderAuditLogRepo) {
        this.limitOrderRepo = limitOrderRepo;
        this.orderAuditLogRepo = orderAuditLogRepo;
    }

    public List<OrderAuditEventResponse> events(long traderId, long orderId) {
        limitOrderRepo.findByIdAndTraderId(orderId, traderId)
                .orElseThrow(() -> new NotFoundException("Order not found."));

        return orderAuditLogRepo.findAllByOrderIdOrderByCreatedAtAsc(orderId)
                .stream()
                .map(event -> new OrderAuditEventResponse(
                        event.getId(),
                        event.getEventType(),
                        event.getMessage(),
                        event.getCreatedAt()
                ))
                .toList();
    }
}