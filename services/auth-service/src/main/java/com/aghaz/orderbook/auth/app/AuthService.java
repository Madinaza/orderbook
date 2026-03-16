package com.aghaz.orderbook.auth.app;

import com.aghaz.orderbook.auth.infra.entity.TraderAccountEntity;
import com.aghaz.orderbook.auth.infra.repo.TraderAccountRepo;
import com.aghaz.orderbook.auth.security.JwtTokenService;
import com.aghaz.orderbook.shared_contracts.auth.UserRole;
import com.aghaz.orderbook.shared_contracts.exceptions.BusinessRuleException;
import com.aghaz.orderbook.shared_contracts.exceptions.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final TraderAccountRepo traderAccountRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(TraderAccountRepo traderAccountRepo,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService) {
        this.traderAccountRepo = traderAccountRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public String register(String username, String password) {
        String normalizedUsername = normalizeUsername(username);

        if (traderAccountRepo.existsByUsername(normalizedUsername)) {
            throw new BusinessRuleException("Username already taken.");
        }

        TraderAccountEntity saved = traderAccountRepo.save(
                new TraderAccountEntity(
                        normalizedUsername,
                        passwordEncoder.encode(password),
                        UserRole.ROLE_TRADER.name()
                )
        );

        log.info("Registered trader account: username={}, id={}, role={}",
                saved.getUsername(), saved.getId(), saved.getRole());

        return jwtTokenService.issue(saved.getId(), saved.getUsername(), saved.getRole());
    }

    @Transactional(readOnly = true)
    public String login(String username, String password) {
        String normalizedUsername = normalizeUsername(username);

        TraderAccountEntity user = traderAccountRepo.findByUsername(normalizedUsername)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials.");
        }

        log.info("Successful login: username={}, id={}, role={}",
                user.getUsername(), user.getId(), user.getRole());

        return jwtTokenService.issue(user.getId(), user.getUsername(), user.getRole());
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new BusinessRuleException("Username is required.");
        }
        return username.trim().toLowerCase();
    }
}