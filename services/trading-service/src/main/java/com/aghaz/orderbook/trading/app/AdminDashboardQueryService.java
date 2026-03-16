package com.aghaz.orderbook.trading.app;

import com.aghaz.orderbook.trading.domain.OrderStatus;
import com.aghaz.orderbook.trading.dto.admin.AdminDashboardResponse;
import com.aghaz.orderbook.trading.dto.admin.InstrumentSummaryRow;
import com.aghaz.orderbook.trading.infra.repo.LimitOrderRepo;
import com.aghaz.orderbook.trading.infra.repo.TradeFillRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
@Transactional(readOnly = true)
public class AdminDashboardQueryService {

    private final LimitOrderRepo limitOrderRepo;
    private final TradeFillRepo tradeFillRepo;

    public AdminDashboardQueryService(LimitOrderRepo limitOrderRepo, TradeFillRepo tradeFillRepo) {
        this.limitOrderRepo = limitOrderRepo;
        this.tradeFillRepo = tradeFillRepo;
    }

    public AdminDashboardResponse summary() {
        long totalOrders = limitOrderRepo.count();
        long openOrders = limitOrderRepo.countByStatus(OrderStatus.NEW)
                + limitOrderRepo.countByStatus(OrderStatus.PARTIALLY_FILLED);
        long filledOrders = limitOrderRepo.countByStatus(OrderStatus.FILLED);
        long cancelledOrders = limitOrderRepo.countByStatus(OrderStatus.CANCELLED);
        long totalTrades = tradeFillRepo.count();
        long activeInstruments = limitOrderRepo.countDistinctInstruments();

        long buyOrders = limitOrderRepo.countBuyOrders();
        long sellOrders = limitOrderRepo.countSellOrders();

        BigDecimal buySellRatio = sellOrders == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(buyOrders)
                .divide(BigDecimal.valueOf(sellOrders), 2, RoundingMode.HALF_UP);

        BigDecimal filledRatePercent = totalOrders == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf((filledOrders * 100.0) / totalOrders)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal cancelRatePercent = totalOrders == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf((cancelledOrders * 100.0) / totalOrders)
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, Long> orderCounts = new HashMap<>();
        for (Object[] row : limitOrderRepo.countOrdersByInstrument()) {
            orderCounts.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }

        Map<String, Long> tradeCounts = new HashMap<>();
        for (Object[] row : tradeFillRepo.countTradesByInstrument()) {
            tradeCounts.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }

        Map<String, Long> openQuantities = new HashMap<>();
        for (Object[] row : limitOrderRepo.sumOpenQuantityByInstrument()) {
            openQuantities.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }

        Set<String> instruments = new TreeSet<>();
        instruments.addAll(orderCounts.keySet());
        instruments.addAll(tradeCounts.keySet());
        instruments.addAll(openQuantities.keySet());

        List<InstrumentSummaryRow> rows = instruments.stream()
                .map(instrument -> new InstrumentSummaryRow(
                        instrument,
                        orderCounts.getOrDefault(instrument, 0L),
                        tradeCounts.getOrDefault(instrument, 0L),
                        openQuantities.getOrDefault(instrument, 0L)
                ))
                .toList();

        List<InstrumentSummaryRow> top5ActiveInstruments = rows.stream()
                .sorted(Comparator.comparingLong(InstrumentSummaryRow::openQuantity).reversed())
                .limit(5)
                .toList();

        List<InstrumentSummaryRow> top5TradedInstruments = rows.stream()
                .sorted(Comparator.comparingLong(InstrumentSummaryRow::trades).reversed())
                .limit(5)
                .toList();

        return new AdminDashboardResponse(
                totalOrders,
                openOrders,
                filledOrders,
                cancelledOrders,
                totalTrades,
                activeInstruments,
                buySellRatio,
                filledRatePercent,
                cancelRatePercent,
                rows,
                top5ActiveInstruments,
                top5TradedInstruments
        );
    }
}