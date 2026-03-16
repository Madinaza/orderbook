package com.aghaz.orderbook.auth.config;

import com.aghaz.orderbook.auth.infra.entity.TraderAccountEntity;
import com.aghaz.orderbook.auth.infra.repo.TraderAccountRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Configuration
@Profile("dev")
public class DemoUserSeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);

    @Bean
    @Transactional
    ApplicationRunner seedDemoUsers(TraderAccountRepo traderAccountRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            List<String> usernames = List.of(
                    "demo_trader_1",
                    "demo_trader_2",
                    "demo_trader_3",
                    "demo_trader_4",
                    "demo_trader_5",
                    "admin_demo"
            );

            int created = 0;

            for (String username : usernames) {
                if (traderAccountRepo.existsByUsername(username)) {
                    continue;
                }

                String role = username.equals("admin_demo") ? "ROLE_ADMIN" : "ROLE_TRADER";

                traderAccountRepo.save(new TraderAccountEntity(
                        username,
                        passwordEncoder.encode("password123"),
                        role
                ));

                created++;
            }

            log.info("Demo user seeding complete. created={}, defaultPassword=password123", created);
        };
    }
}