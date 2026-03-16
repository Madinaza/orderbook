package com.aghaz.orderbook.auth.api;

import com.aghaz.orderbook.auth.app.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public TokenResponse register(@Valid @RequestBody Credentials request) {
        String token = authService.register(request.username(), request.password());
        return new TokenResponse(token);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody Credentials request) {
        String token = authService.login(request.username(), request.password());
        return new TokenResponse(token);
    }

    public record Credentials(
            @NotBlank @Size(min = 3, max = 60) String username,
            @NotBlank @Size(min = 6, max = 120) String password
    ) {}

    public record TokenResponse(String token) {}
}