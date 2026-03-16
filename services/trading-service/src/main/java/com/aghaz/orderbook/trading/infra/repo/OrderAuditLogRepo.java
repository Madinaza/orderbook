package com.aghaz.orderbook.trading.infra.repo;

import com.aghaz.orderbook.trading.infra.entity.OrderAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderAuditLogRepo extends JpaRepository<OrderAuditLogEntity, Long> {
    List<OrderAuditLogEntity> findAllByOrderIdOrderByCreatedAtAsc(long orderId);
}