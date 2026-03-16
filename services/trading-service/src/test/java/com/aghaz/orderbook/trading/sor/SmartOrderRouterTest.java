package com.aghaz.orderbook.trading.sor;

import com.aghaz.orderbook.trading.domain.Side;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SmartOrderRouterTest {

    @Test
    void bestVenue_shouldChooseLowestEffectiveCost_forBuy() {
        SmartOrderRouter router = new SmartOrderRouter();

        ExchangeQuote lse = new ExchangeQuote(
                "LSE",
                "AAPL",
                new BigDecimal("100.00"),
                100,
                new BigDecimal("0.0020")
        );

        ExchangeQuote nyse = new ExchangeQuote(
                "NYSE",
                "AAPL",
                new BigDecimal("99.95"),
                100,
                new BigDecimal("0.0030")
        );

        ExchangeQuote best = router.bestVenue(Side.BUY, List.of(lse, nyse), 100).orElseThrow();

        assertEquals("LSE", best.exchangeCode());
    }

    @Test
    void bestVenue_shouldChooseHighestNetProceeds_forSell() {
        SmartOrderRouter router = new SmartOrderRouter();

        ExchangeQuote lse = new ExchangeQuote(
                "LSE",
                "AAPL",
                new BigDecimal("100.10"),
                100,
                new BigDecimal("0.0030")
        );

        ExchangeQuote nyse = new ExchangeQuote(
                "NYSE",
                "AAPL",
                new BigDecimal("100.05"),
                100,
                new BigDecimal("0.0010")
        );

        ExchangeQuote best = router.bestVenue(Side.SELL, List.of(lse, nyse), 100).orElseThrow();

        assertEquals("NYSE", best.exchangeCode());
    }

    @Test
    void bestVenue_shouldReturnEmpty_whenNoVenueHasEnoughLiquidity() {
        SmartOrderRouter router = new SmartOrderRouter();

        ExchangeQuote lse = new ExchangeQuote(
                "LSE",
                "AAPL",
                new BigDecimal("100.00"),
                50,
                new BigDecimal("0.0010")
        );

        ExchangeQuote nyse = new ExchangeQuote(
                "NYSE",
                "AAPL",
                new BigDecimal("99.90"),
                60,
                new BigDecimal("0.0015")
        );

        assertTrue(router.bestVenue(Side.BUY, List.of(lse, nyse), 100).isEmpty());
    }

    @Test
    void effectiveBuyCost_shouldIncludeFees() {
        SmartOrderRouter router = new SmartOrderRouter();

        ExchangeQuote quote = new ExchangeQuote(
                "NASDAQ",
                "AAPL",
                new BigDecimal("100.00"),
                100,
                new BigDecimal("0.0025")
        );

        BigDecimal result = router.effectiveBuyCost(quote, 10);

        assertEquals(new BigDecimal("1002.500000"), result);
    }

    @Test
    void effectiveSellProceeds_shouldSubtractFees() {
        SmartOrderRouter router = new SmartOrderRouter();

        ExchangeQuote quote = new ExchangeQuote(
                "NASDAQ",
                "AAPL",
                new BigDecimal("100.00"),
                100,
                new BigDecimal("0.0025")
        );

        BigDecimal result = router.effectiveSellProceeds(quote, 10);

        assertEquals(new BigDecimal("997.500000"), result);
    }
}