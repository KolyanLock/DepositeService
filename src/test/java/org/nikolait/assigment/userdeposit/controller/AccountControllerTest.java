package org.nikolait.assigment.userdeposit.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nikolait.assigment.userdeposit.IntegrationTestBase;
import org.nikolait.assigment.userdeposit.dto.TransferRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.nikolait.assigment.userdeposit.util.TestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerTest extends IntegrationTestBase {

    @Test
    @DisplayName("Получение данных аккаунта текущего пользователя")
    void getMyAccount() throws Exception {
        mockMvc.perform(get("/api/v1/user/account/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deposit").value(USER1_DEPOSIT))
                .andExpect(jsonPath("$.capitalization").value(USER1_CAPITALIZATION))
                .andExpect(jsonPath("$.balance").value(USER1_BALANCE));
    }

    @Test
    @DisplayName("Перевод средств от user1 к user2")
    void transferFunds_success() throws Exception {
        BigDecimal transferAmount = BigDecimal.valueOf(100.00);

        // Выполняем перевод от user1 к user2
        mockMvc.perform(post("/api/v1/user/account/transfer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferRequest(user2Id, transferAmount))))
                .andExpect(status().isOk());

        // Проверяем баланс отправителя
        mockMvc.perform(get("/api/v1/user/account/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance")
                        .value(USER1_BALANCE - transferAmount.doubleValue()));

        // Проверяем баланс получателя
        mockMvc.perform(get("/api/v1/user/account/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user2AccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance")
                        .value(USER2_BALANCE + transferAmount.doubleValue()));
    }

    @Test
    @DisplayName("Перевод средств самому себе должен быть отклонён")
    void transferFunds_toSelf() throws Exception {
        BigDecimal transferAmount = BigDecimal.valueOf(50.00);

        mockMvc.perform(post("/api/v1/user/account/transfer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferRequest(user1Id, transferAmount))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Перевод суммы больше баланса должен быть отклонён")
    void transferFunds_insufficientBalance() throws Exception {
        BigDecimal transferAmount = BigDecimal.valueOf(USER1_BALANCE + 0.01);

        mockMvc.perform(post("/api/v1/user/account/transfer")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferRequest(user2Id, transferAmount))))
                .andExpect(status().isBadRequest());
    }

}
