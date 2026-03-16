package com.aghaz.orderbook.trading.app;

import com.aghaz.orderbook.shared_contracts.exceptions.BusinessRuleException;
import com.aghaz.orderbook.shared_contracts.exceptions.NotFoundException;
import com.aghaz.orderbook.shared_contracts.messaging.OrderChangedEvent;
import com.aghaz.orderbook.shared_contracts.messaging.TradeExecutedEvent;
import com.aghaz.orderbook.trading.domain.*;
import com.aghaz.orderbook.trading.dto.OrderResponse;
import com.aghaz.orderbook.trading.dto.PlaceOrderRequest;
import com.aghaz.orderbook.trading.dto.ReplaceOrderRequest;
import com.aghaz.orderbook.trading.infra.entity.LimitOrderEntity;
import com.aghaz.orderbook.trading.infra.entity.TradeFillEntity;
import com.aghaz.orderbook.trading.infra.repo.LimitOrderRepo;
import com.aghaz.orderbook.trading.infra.repo.TradeFillRepo;
import com.aghaz.orderbook.trading.messaging.TradingEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
public class OrderCommandService {

    private static final Logger log = LoggerFactory.getLogger(OrderCommandService.class);
    private static final EnumSet<OrderStatus> LIVE = EnumSet.of(OrderStatus.NEW, OrderStatus.PARTIALLY_FILLED);

    private final LimitOrderRepo limitOrderRepo;
    private final TradeFillRepo tradeFillRepo;
    private final TradingEventPublisher eventPublisher;
    private final OrderAuditService orderAuditService;
    private final MatchingEngine matchingEngine = new MatchingEngine();

    public OrderCommandService(LimitOrderRepo limitOrderRepo,
                               TradeFillRepo tradeFillRepo,
                               TradingEventPublisher eventPublisher,
                               OrderAuditService orderAuditService) {
        this.limitOrderRepo = limitOrderRepo;
        this.tradeFillRepo = tradeFillRepo;
        this.eventPublisher = eventPublisher;
        this.orderAuditService = orderAuditService;
    }

    @Transactional
    public OrderResponse place(long traderId, PlaceOrderRequest request) {
        String instrument = normalizeInstrument(request.instrument());
        validatePlaceRequest(request);

        LimitOrderEntity incomingEntity = limitOrderRepo.save(
                LimitOrderEntity.place(
                        traderId,
                        instrument,
                        request.side(),
                        request.orderType(),
                        request.limitPrice(),
                        request.quantity()
                )
        );

        orderAuditService.recordPlaced(incomingEntity);

        OrderBook orderBook = rebuildBook(instrument, incomingEntity.getId());
        BookOrder incomingDomain = toDomain(incomingEntity);

        List<TradeFill> fills = matchingEngine.match(orderBook, incomingDomain);

        long newlyFilledOnIncoming = incomingEntity.getOpenQty() - incomingDomain.openQty();

        syncIncomingEntity(incomingEntity, incomingDomain);
        incomingEntity = limitOrderRepo.save(incomingEntity);
        publishOrderChanged(incomingEntity);

        if (newlyFilledOnIncoming > 0) {
            orderAuditService.recordFill(incomingEntity, newlyFilledOnIncoming);
        }

        for (TradeFill fill : fills) {
            TradeFillEntity tradeEntity = toTradeFillEntity(fill);
            tradeFillRepo.save(tradeEntity);
            publishTradeExecuted(tradeEntity);

            applyFillToRestingOrder(fill.buyOrderId(), fill.quantity(), incomingEntity.getId());
            applyFillToRestingOrder(fill.sellOrderId(), fill.quantity(), incomingEntity.getId());
        }

        log.info(
                "Placed/matched order. orderId={}, clientOrderId={}, traderId={}, instrument={}, side={}, type={}, fills={}",
                incomingEntity.getId(),
                incomingEntity.getClientOrderId(),
                traderId,
                instrument,
                incomingEntity.getSide(),
                incomingEntity.getOrderType(),
                fills.size()
        );

        return toResponse(incomingEntity);
    }

    @Transactional
    public OrderResponse cancel(long traderId, long orderId) {
        LimitOrderEntity entity = loadOwnedOrder(traderId, orderId);

        try {
            entity.cancel();
            LimitOrderEntity saved = limitOrderRepo.save(entity);
            publishOrderChanged(saved);
            orderAuditService.recordCancelled(saved);

            log.info("Cancelled order. orderId={}, traderId={}", orderId, traderId);
            return toResponse(saved);

        } catch (IllegalStateException ex) {
            throw new BusinessRuleException(ex.getMessage());

        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new BusinessRuleException("Order was modified concurrently. Please refresh and try again.");
        }
    }

    @Transactional
    public OrderResponse replace(long traderId, long orderId, ReplaceOrderRequest request) {
        LimitOrderEntity entity = loadOwnedOrder(traderId, orderId);

        try {
            BigDecimal oldPrice = entity.getLimitPrice();
            long oldQty = entity.getOriginalQty();

            entity.replace(request.newLimitPrice(), request.newQuantity());
            LimitOrderEntity saved = limitOrderRepo.save(entity);
            publishOrderChanged(saved);
            orderAuditService.recordReplaced(saved, oldPrice, oldQty);

            log.info(
                    "Replaced order. orderId={}, traderId={}, newPrice={}, newQty={}",
                    orderId,
                    traderId,
                    request.newLimitPrice(),
                    request.newQuantity()
            );

            return toResponse(saved);

        } catch (IllegalStateException | IllegalArgumentException ex) {
            throw new BusinessRuleException(ex.getMessage());

        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new BusinessRuleException("Order was modified concurrently. Please refresh and try again.");
        }
    }

    private OrderBook rebuildBook(String instrument, Long excludeOrderId) {
        OrderBook book = new OrderBook(instrument);

        List<LimitOrderEntity> liveOrders = limitOrderRepo.findAllByInstrumentAndStatusIn(instrument, LIVE);

        for (LimitOrderEntity entity : liveOrders) {
            if (excludeOrderId != null && excludeOrderId.equals(entity.getId())) {
                continue;
            }
            if (entity.getOrderType() != OrderType.LIMIT) {
                continue;
            }
            book.addResting(toDomain(entity));
        }

        return book;
    }

    private BookOrder toDomain(LimitOrderEntity entity) {
        PriceIntent intent = entity.getOrderType() == OrderType.MARKET
                ? new PriceIntent.Market()
                : new PriceIntent.Limit(entity.getLimitPrice());

        BookOrder order = new BookOrder(
                entity.getId(),
                entity.getTraderId(),
                entity.getInstrument(),
                entity.getSide(),
                intent,
                entity.getOriginalQty(),
                entity.getCreatedAt()
        );

        long alreadyFilled = entity.filledQty();
        if (alreadyFilled > 0) {
            order.applyFill(alreadyFilled);
        }

        return order;
    }

    private void syncIncomingEntity(LimitOrderEntity incomingEntity, BookOrder incomingDomain) {
        long filledQty = incomingEntity.getOriginalQty() - incomingDomain.openQty();
        if (filledQty > 0) {
            incomingEntity.applyFill(filledQty);
        }
    }

    private void applyFillToRestingOrder(long orderId, long quantity, Long incomingOrderId) {
        if (orderId == incomingOrderId) {
            return;
        }

        limitOrderRepo.findById(orderId).ifPresent(entity -> {
            if (!entity.isLive()) {
                return;
            }

            entity.applyFill(quantity);
            LimitOrderEntity saved = limitOrderRepo.save(entity);
            publishOrderChanged(saved);
            orderAuditService.recordFill(saved, quantity);
        });
    }

    private TradeFillEntity toTradeFillEntity(TradeFill fill) {
        return new TradeFillEntity(
                fill.instrument(),
                fill.buyOrderId(),
                fill.sellOrderId(),
                fill.buyTraderId(),
                fill.sellTraderId(),
                fill.price(),
                fill.quantity(),
                fill.executedAt()
        );
    }

    private void publishOrderChanged(LimitOrderEntity entity) {
        eventPublisher.orderChanged(new OrderChangedEvent(
                UUID.randomUUID().toString(),
                entity.getId(),
                entity.getTraderId(),
                entity.getInstrument(),
                entity.getSide().name(),
                entity.getOrderType().name(),
                entity.getLimitPrice(),
                entity.getOriginalQty(),
                entity.getOpenQty(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        ));
    }

    private void publishTradeExecuted(TradeFillEntity entity) {
        eventPublisher.tradeExecuted(new TradeExecutedEvent(
                UUID.randomUUID().toString(),
                entity.getInstrument(),
                entity.getBuyOrderId(),
                entity.getSellOrderId(),
                entity.getPrice(),
                entity.getQuantity(),
                entity.getExecutedAt()
        ));
    }

    private LimitOrderEntity loadOwnedOrder(long traderId, long orderId) {
        return limitOrderRepo.findByIdAndTraderId(orderId, traderId)
                .orElseThrow(() -> new NotFoundException("Order not found."));
    }

    private void validatePlaceRequest(PlaceOrderRequest request) {
        if (request.quantity() <= 0) {
            throw new BusinessRuleException("Quantity must be greater than zero.");
        }

        if (request.orderType() == OrderType.LIMIT) {
            if (request.limitPrice() == null || request.limitPrice().signum() <= 0) {
                throw new BusinessRuleException("Limit price is required and must be positive for LIMIT orders.");
            }
        }
    }

    private String normalizeInstrument(String instrument) {
        if (instrument == null || instrument.isBlank()) {
            throw new BusinessRuleException("Instrument is required.");
        }
        return instrument.trim().toUpperCase();
    }

    private OrderResponse toResponse(LimitOrderEntity entity) {
        return new OrderResponse(
                entity.getId(),
                entity.getClientOrderId(),
                entity.getTraderId(),
                entity.getInstrument(),
                entity.getSide().name(),
                entity.getOrderType().name(),
                entity.getLimitPrice(),
                entity.getOriginalQty(),
                entity.getOpenQty(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}