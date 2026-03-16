package com.aghaz.orderbook.auth.api;

import com.aghaz.orderbook.auth.AuthServiceApplication;
import com.aghaz.orderbook.auth.infra.repo.TraderAccountRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API-level tests (fast, no Docker) that validate:
 * - validation errors return consistent ApiError
 * - duplicate username returns BusinessRuleException mapped to 400
 */
@SpringBootTest(classes = AuthServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerApiTest {

    @Autowired MockMvc mvc;
    @Autowired TraderAccountRepo repo;

    @Test
    void register_shouldReturn400_whenUsernameTooShort() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"ab","password":"password123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("username")))
                .andExpect(jsonPath("$.path", is("/api/auth/register")));
    }

    @Test
    void register_shouldReturn400_whenUsernameDuplicate() throws Exception {
        // First registration OK
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"demo","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankString())));

        // Second registration should fail with business rule error
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"demo","password":"password123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Username already taken.")));
    }
}
