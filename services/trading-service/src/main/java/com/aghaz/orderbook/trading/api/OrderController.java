package com.aghaz.orderbook.trading.api;

import com.aghaz.orderbook.shared_contracts.api.PagedResponse;
import com.aghaz.orderbook.trading.app.OrderAuditQueryService;
import com.aghaz.orderbook.trading.app.OrderCommandService;
import com.aghaz.orderbook.trading.app.OrderQueryService;
import com.aghaz.orderbook.trading.dto.OrderAuditEventResponse;
import com.aghaz.orderbook.trading.dto.OrderResponse;
import com.aghaz.orderbook.trading.dto.PlaceOrderRequest;
import com.aghaz.orderbook.trading.dto.ReplaceOrderRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderCommandService commands;
    private final OrderQueryService queries;
    private final OrderAuditQueryService auditQueries;

    public OrderController(OrderCommandService commands,
                           OrderQueryService queries,
                           OrderAuditQueryService auditQueries) {
        this.commands = commands;
        this.queries = queries;
        this.auditQueries = auditQueries;
    }

    @PostMapping
    public OrderResponse place(@Valid @RequestBody PlaceOrderRequest req, Authentication auth) {
        long traderId = traderId(auth);
        return commands.place(traderId, req);
    }

    @GetMapping
    public PagedResponse<OrderResponse> myOrders(
            Authentication auth,
            @RequestParam(required = false) String instrument,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String side,
            @RequestParam(required = false) String orderType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        long traderId = traderId(auth);
        return queries.myOrders(traderId, instrument, status, side, orderType, page, size, sortBy, sortDirection);
    }

    @GetMapping("/{id}")
    public OrderResponse byId(@PathVariable long id, Authentication auth) {
        long traderId = traderId(auth);
        return queries.byId(traderId, id);
    }

    @GetMapping("/{id}/events")
    public List<OrderAuditEventResponse> events(@PathVariable long id, Authentication auth) {
        long traderId = traderId(auth);
        return auditQueries.events(traderId, id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable long id, Authentication auth) {
        long traderId = traderId(auth);
        return commands.cancel(traderId, id);
    }

    @PutMapping("/{id}")
    public OrderResponse replace(@PathVariable long id,
                                 @Valid @RequestBody ReplaceOrderRequest req,
                                 Authentication auth) {
        long traderId = traderId(auth);
        return commands.replace(traderId, id, req);
    }

    private long traderId(Authentication auth) {
        Object p = auth.getPrincipal();
        if (p instanceof Long l) return l;
        if (p instanceof Integer i) return i.longValue();
        if (p instanceof String s) return Long.parseLong(s);
        throw new IllegalStateException("Unexpected principal type: " + p);
    }
}