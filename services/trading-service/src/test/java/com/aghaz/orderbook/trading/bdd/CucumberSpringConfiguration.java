package com.aghaz.orderbook.trading.bdd;

import com.aghaz.orderbook.trading.TradingServiceApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(classes = TradingServiceApplication.class)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}