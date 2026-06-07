package com.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WalletIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static String userAToken;
    private static String userBToken;
    private static String userBWalletNumber;

    @Test
    @Order(1)
    @DisplayName("Register User A and receive JWT")
    void registerUserA() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Alice Smith")
                .email("alice@test.com")
                .password("password123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        userAToken = objectMapper.readTree(body).path("data").path("token").asText();
        assertThat(userAToken).isNotBlank();
    }

    @Test
    @Order(2)
    @DisplayName("Register User B and retrieve wallet number")
    void registerUserB() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Bob Jones")
                .email("bob@test.com")
                .password("password123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        userBToken = objectMapper.readTree(body).path("data").path("token").asText();

        MvcResult walletResult = mockMvc.perform(get("/api/wallets/me")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andReturn();

        String walletBody = walletResult.getResponse().getContentAsString();
        userBWalletNumber = objectMapper.readTree(walletBody).path("data").path("walletNumber").asText();
        assertThat(userBWalletNumber).startsWith("WLT-");
    }

    @Test
    @Order(3)
    @DisplayName("Get my wallet - returns ACTIVE with zero balance")
    void getMyWallet() throws Exception {
        mockMvc.perform(get("/api/wallets/me")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.balance").value(0.0));
    }

    @Test
    @Order(4)
    @DisplayName("Transfer fails: insufficient balance")
    void transferFailsInsufficientBalance() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .receiverWalletNumber(userBWalletNumber)
                .amount(new BigDecimal("500.00"))
                .note("Test")
                .build();

        mockMvc.perform(post("/api/transactions/transfer")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Insufficient")));
    }

    @Test
    @Order(5)
    @DisplayName("Transfer fails: negative amount fails validation")
    void transferFailsValidation() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .receiverWalletNumber(userBWalletNumber)
                .amount(new BigDecimal("-100.00"))
                .build();

        mockMvc.perform(post("/api/transactions/transfer")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    @DisplayName("Get my transactions - returns empty list for new user")
    void getMyTransactions() throws Exception {
        mockMvc.perform(get("/api/transactions/me")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(7)
    @DisplayName("Unauthenticated request returns 401")
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/wallets/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("Actuator health endpoint is accessible without auth")
    void actuatorHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @Order(9)
    @DisplayName("Registration fails with duplicate email - 409 Conflict")
    void registerDuplicateEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Duplicate Alice")
                .email("alice@test.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(10)
    @DisplayName("Registration fails: invalid email format returns field errors")
    void registerInvalidEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test User")
                .email("not-an-email")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.email").exists());
    }
}
