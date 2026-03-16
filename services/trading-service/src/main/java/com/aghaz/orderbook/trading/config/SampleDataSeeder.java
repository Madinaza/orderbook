package com.aghaz.orderbook.trading.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Configuration
@Profile("dev")
public class SampleDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(SampleDataSeeder.class);

    private static final List<String> INSTRUMENTS = List.of(
            "AAPL", "MSFT", "NVDA", "TSLA", "AMZN",
            "GOOGL", "META", "EURUSD", "BTCUSD", "ETHUSD"
    );

    private static final Map<String, BigDecimal> BASE_PRICES = Map.of(
            "AAPL", new BigDecimal("182.50"),
            "MSFT", new BigDecimal("415.80"),
            "NVDA", new BigDecimal("905.40"),
            "TSLA", new BigDecimal("212.10"),
            "AMZN", new BigDecimal("178.30"),
            "GOOGL", new BigDecimal("161.20"),
            "META", new BigDecimal("498.60"),
            "EURUSD", new BigDecimal("1.084200"),
            "BTCUSD", new BigDecimal("67250.000000"),
            "ETHUSD", new BigDecimal("3520.000000")
    );

    @Bean
    @Transactional
    ApplicationRunner seedSampleData(
            JdbcTemplate jdbcTemplate,
            @Value("${app.seed.enabled:true}") boolean seedEnabled
    ) {
        return args -> {
            if (!seedEnabled) {
                log.info("Sample data seeding disabled.");
                return;
            }

            Integer existingOrders = jdbcTemplate.queryForObject(
                    "select count(*) from limit_order",
                    Integer.class
            );

            if (existingOrders != null && existingOrders > 0) {
                log.info("Sample data skipped because limit_order already contains {} rows.", existingOrders);
                return;
            }

            Random random = new Random(42);

            List<SeedOrder> seededOrders = seedOrders(jdbcTemplate, random, 1200);
            int seededTrades = seedTrades(jdbcTemplate, random, seededOrders, 320);

            log.info("Sample data seeding complete. orders={}, trades={}", seededOrders.size(), seededTrades);
            log.info("Demo traders expected in auth-service: demo_trader_1 .. demo_trader_5, password=password123");
        };
    }

    private List<SeedOrder> seedOrders(JdbcTemplate jdbcTemplate, Random random, int totalOrders) {
        List<SeedOrder> orders = new ArrayList<>(totalOrders);

        for (int i = 0; i < totalOrders; i++) {
            String instrument = pick(INSTRUMENTS, random);
            String side = random.nextBoolean() ? "BUY" : "SELL";
            String orderType = "LIMIT";

            BigDecimal base = BASE_PRICES.get(instrument);
            BigDecimal price = randomPrice(base, instrument, random);
            long originalQty = randomQuantity(instrument, random);

            String status = randomStatus(random);
            long openQty = switch (status) {
                case "NEW" -> originalQty;
                case "PARTIALLY_FILLED" -> Math.max(1, originalQty - random.nextInt((int) Math.max(1, originalQty - 1)));
                case "FILLED", "CANCELLED" -> 0L;
                default -> originalQty;
            };

            Instant createdAt = Instant.now()
                    .minus(random.nextInt(25), ChronoUnit.DAYS)
                    .minus(random.nextInt(24), ChronoUnit.HOURS)
                    .minus(random.nextInt(60), ChronoUnit.MINUTES);

            Instant updatedAt = createdAt.plus(random.nextInt(360), ChronoUnit.MINUTES);

            String clientOrderId = UUID.randomUUID().toString();
            long traderId = 1 + random.nextInt(5);

            Long id = jdbcTemplate.queryForObject("""
                    insert into limit_order (
                        client_order_id,
                        trader_id,
                        instrument,
                        side,
                        order_type,
                        limit_price,
                        original_qty,
                        open_qty,
                        status,
                        created_at,
                        updated_at,
                        version
                    )
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                    returning id
                    """,
                    Long.class,
                    clientOrderId,
                    traderId,
                    instrument,
                    side,
                    orderType,
                    price,
                    originalQty,
                    openQty,
                    status,
                    Timestamp.from(createdAt),
                    Timestamp.from(updatedAt)
            );

            long filledQty = originalQty - openQty;

            orders.add(new SeedOrder(
                    Objects.requireNonNull(id),
                    traderId,
                    instrument,
                    side,
                    price,
                    originalQty,
                    openQty,
                    filledQty,
                    createdAt
            ));
        }

        return orders;
    }

    private int seedTrades(JdbcTemplate jdbcTemplate, Random random, List<SeedOrder> orders, int targetTrades) {
        Map<String, List<SeedOrder>> buyByInstrument = new HashMap<>();
        Map<String, List<SeedOrder>> sellByInstrument = new HashMap<>();

        for (SeedOrder order : orders) {
            if (order.filledQty() <= 0) {
                continue;
            }

            if (order.side().equals("BUY")) {
                buyByInstrument.computeIfAbsent(order.instrument(), ignored -> new ArrayList<>()).add(order);
            } else {
                sellByInstrument.computeIfAbsent(order.instrument(), ignored -> new ArrayList<>()).add(order);
            }
        }

        int tradesInserted = 0;
        int attempts = 0;
        int maxAttempts = 5000;

        while (tradesInserted < targetTrades && attempts < maxAttempts) {
            attempts++;

            String instrument = pick(INSTRUMENTS, random);
            List<SeedOrder> buys = buyByInstrument.getOrDefault(instrument, List.of());
            List<SeedOrder> sells = sellByInstrument.getOrDefault(instrument, List.of());

            if (buys.isEmpty() || sells.isEmpty()) {
                continue;
            }

            SeedOrder buy = buys.get(random.nextInt(buys.size()));
            SeedOrder sell = sells.get(random.nextInt(sells.size()));

            if (buy.traderId() == sell.traderId()) {
                continue;
            }

            long maxQty = Math.min(buy.filledQty(), sell.filledQty());
            if (maxQty <= 0) {
                continue;
            }

            long tradeQty = Math.max(1, Math.min(maxQty, 1 + random.nextInt((int) Math.min(200, maxQty))));
            BigDecimal tradePrice = buy.price()
                    .add(sell.price())
                    .divide(new BigDecimal("2"), 6, RoundingMode.HALF_UP);

            Instant executedAt = maxInstant(buy.createdAt(), sell.createdAt())
                    .plus(random.nextInt(720), ChronoUnit.MINUTES);

            jdbcTemplate.update("""
                    insert into trade_fill (
                        instrument,
                        buy_order_id,
                        sell_order_id,
                        buy_trader_id,
                        sell_trader_id,
                        price,
                        quantity,
                        executed_at
                    )
                    values (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    instrument,
                    buy.id(),
                    sell.id(),
                    buy.traderId(),
                    sell.traderId(),
                    tradePrice,
                    tradeQty,
                    Timestamp.from(executedAt)
            );

            tradesInserted++;
        }

        return tradesInserted;
    }

    private String randomStatus(Random random) {
        int n = random.nextInt(100);
        if (n < 38) return "NEW";
        if (n < 68) return "PARTIALLY_FILLED";
        if (n < 88) return "FILLED";
        return "CANCELLED";
    }

    private long randomQuantity(String instrument, Random random) {
        return switch (instrument) {
            case "BTCUSD", "ETHUSD" -> 1 + random.nextInt(8);
            case "EURUSD" -> 1_000 + random.nextInt(20_000);
            default -> 10 + random.nextInt(400);
        };
    }

    private BigDecimal randomPrice(BigDecimal base, String instrument, Random random) {
        double drift = 1 + ((random.nextDouble() - 0.5) * 0.08);
        int scale = instrument.equals("EURUSD") || instrument.equals("BTCUSD") || instrument.equals("ETHUSD") ? 6 : 2;
        return base.multiply(BigDecimal.valueOf(drift)).setScale(scale, RoundingMode.HALF_UP);
    }

    private <T> T pick(List<T> items, Random random) {
        return items.get(random.nextInt(items.size()));
    }

    private Instant maxInstant(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }

    private record SeedOrder(
            long id,
            long traderId,
            String instrument,
            String side,
            BigDecimal price,
            long originalQty,
            long openQty,
            long filledQty,
            Instant createdAt
    ) {
    }
}