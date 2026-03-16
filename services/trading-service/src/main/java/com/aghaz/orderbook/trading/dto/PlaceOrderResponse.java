package com.aghaz.orderbook.trading.dto;

import com.aghaz.orderbook.trading.domain.OrderStatus;

public record PlaceOrderResponse(
        Long orderId,
        OrderStatus status,
        long remainingQty,
        int trades
) {}