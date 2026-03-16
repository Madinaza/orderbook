package com.aghaz.orderbook.trading.app;

import com.aghaz.orderbook.shared_contracts.exceptions.BusinessRuleException;
import com.aghaz.orderbook.trading.domain.Side;
import com.aghaz.orderbook.trading.dto.admin.BestVenueRequest;
import com.aghaz.orderbook.trading.dto.admin.BestVenueResponse;
import com.aghaz.orderbook.trading.sor.ExchangeQuote;
import com.aghaz.orderbook.trading.sor.FeeLadder;
import com.aghaz.orderbook.trading.sor.SmartOrderRouter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AdminRoutingService {

    private final SmartOrderRouter router = new SmartOrderRouter();
    private final FeeLadder feeLadder = new FeeLadder();

    public BestVenueResponse simulateBestVenue(BestVenueRequest request) {
        Side side = parseSide(request.side());
        String instrument = normalizeInstrument(request.instrument());
        long quantity = request.quantity();

        List<ExchangeQuote> quotes = request.quotes().stream()
                .map(q -> new ExchangeQuote(
                        q.exchangeCode(),
                        q.instrument(),
                        q.price(),
                        q.availableQuantity(),
                        q.feeRate()
                ))
                .filter(q -> instrument.equals(q.instrument()))
                .toList();

        if (quotes.isEmpty()) {
            throw new BusinessRuleException("No quotes available for instrument " + instrument + ".");
        }

        List<BestVenueResponse.VenueDecision> evaluated = quotes.stream()
                .map(q -> toDecision(q, quantity, side))
                .toList();

        ExchangeQuote bestQuote = router.bestVenue(side, quotes, quantity)
                .orElseThrow(() -> new BusinessRuleException(
                        "No venue has sufficient available quantity for quantity=" + quantity + "."
                ));

        BestVenueResponse.VenueDecision bestDecision = toDecision(bestQuote, quantity, side);

        return new BestVenueResponse(
                instrument,
                side.name(),
                quantity,
                bestDecision,
                evaluated
        );
    }

    private BestVenueResponse.VenueDecision toDecision(ExchangeQuote quote, long quantity, Side side) {
        BigDecimal totalNotional = feeLadder.notional(quote.price(), quantity);
        BigDecimal feeAmount = feeLadder.feeAmount(totalNotional, quote.feeRate());
        BigDecimal effectiveValue = side == Side.BUY
                ? feeLadder.effectiveBuyCost(quote.price(), quantity, quote.feeRate())
                : feeLadder.effectiveSellProceeds(quote.price(), quantity, quote.feeRate());

        return new BestVenueResponse.VenueDecision(
                quote.exchangeCode(),
                quote.instrument(),
                quote.price(),
                quote.availableQuantity(),
                quote.feeRate(),
                totalNotional,
                feeAmount,
                effectiveValue
        );
    }

    private Side parseSide(String side) {
        try {
            return Side.valueOf(side.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BusinessRuleException("Side must be BUY or SELL.");
        }
    }

    private String normalizeInstrument(String instrument) {
        if (instrument == null || instrument.isBlank()) {
            throw new BusinessRuleException("Instrument is required.");
        }
        return instrument.trim().toUpperCase(Locale.ROOT);
    }
}