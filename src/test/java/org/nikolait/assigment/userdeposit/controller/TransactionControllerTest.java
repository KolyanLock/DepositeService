package org.nikolait.assigment.userdeposit.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nikolait.assigment.userdeposit.IntegrationTestBase;
import org.nikolait.assigment.userdeposit.dto.TransactionRequest;
import org.nikolait.assigment.userdeposit.dto.TransferRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.nikolait.assigment.userdeposit.emum.TransactionStatus.*;
import static org.nikolait.assigment.userdeposit.emum.TransactionType.TRANSFER;
import static org.nikolait.assigment.userdeposit.util.TestConstants.USER1_BALANCE;
import static org.nikolait.assigment.userdeposit.util.TestConstants.USER2_BALANCE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerTest extends IntegrationTestBase {

    @Test
    @DisplayName("Перевод средств от user1 к user2")
    void transferFunds_success() throws Exception {
        BigDecimal transferAmount = BigDecimal.valueOf(100.00);

        // Инициируем транзакцию для перевода средств от user1 к user2
        MvcResult result = mockMvc.perform(post("/api/v1/transaction/transfer/init")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferRequest(user2Id, transferAmount))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", aMapWithSize(8)))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.fromUserId").value(user1Id))
                .andExpect(jsonPath("$.toUserId").value(user2Id))
                .andExpect(jsonPath("$.amount").value(transferAmount))
                .andExpect(jsonPath("$.type").value(TRANSFER.name()))
                .andExpect(jsonPath("$.status").value(PENDING.name()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.completedAt", nullValue()))
                .andReturn();

        Long transactionId = JsonPath.parse(result.getResponse().getContentAsString()).read("$.id", Long.class);

        // Обрабатываем транзакцию для перевода средств от user1 к user2
        mockMvc.perform(post("/api/v1/transaction/transfer/commit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransactionRequest(transactionId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", aMapWithSize(8)))
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.fromUserId").value(user1Id))
                .andExpect(jsonPath("$.toUserId").value(user2Id))
                .andExpect(jsonPath("$.amount").value(transferAmount))
                .andExpect(jsonPath("$.type").value(TRANSFER.name()))
                .andExpect(jsonPath("$.status").value(COMPLETED.name()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.completedAt", notNullValue()));

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
    @DisplayName("Транзакция когда не достаточно средств должна завершится со статусом FAILED")
    void transactionFails_dueToInsufficientBalance() throws Exception {
        BigDecimal transferAmount1 = BigDecimal.valueOf(USER1_BALANCE);
        BigDecimal transferAmount2 = BigDecimal.valueOf(100.00);

        // Инициируем 1-ю транзакцию для перевода средств от user1 к user2
        MvcResult result1 = mockMvc.perform(post("/api/v1/transaction/transfer/init")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferRequest(user2Id, transferAmount1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", aMapWithSize(8)))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.fromUserId").value(user1Id))
                .andExpect(jsonPath("$.toUserId").value(user2Id))
                .andExpect(jsonPath("$.amount").value(transferAmount1))
                .andExpect(jsonPath("$.type").value(TRANSFER.name()))
                .andExpect(jsonPath("$.status").value(PENDING.name()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.completedAt", nullValue()))
                .andReturn();

        Long transaction1Id = JsonPath.parse(result1.getResponse().getContentAsString()).read("$.id", Long.class);

        // Инициируем 2-ю транзакцию для перевода средств от user1 к user2
        MvcResult result2 = mockMvc.perform(post("/api/v1/transaction/transfer/init")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferRequest(user2Id, transferAmount2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", aMapWithSize(8)))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.fromUserId").value(user1Id))
                .andExpect(jsonPath("$.toUserId").value(user2Id))
                .andExpect(jsonPath("$.amount").value(transferAmount2))
                .andExpect(jsonPath("$.type").value(TRANSFER.name()))
                .andExpect(jsonPath("$.status").value(PENDING.name()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.completedAt", nullValue()))
                .andReturn();

        Long transaction2Id = JsonPath.parse(result2.getResponse().getContentAsString()).read("$.id", Long.class);

        // Обрабатываем 1-ю транзакцию для перевода всех средств user1 к user2
        mockMvc.perform(post("/api/v1/transaction/transfer/commit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransactionRequest(transaction1Id))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", aMapWithSize(8)))
                .andExpect(jsonPath("$.id").value(transaction1Id))
                .andExpect(jsonPath("$.fromUserId").value(user1Id))
                .andExpect(jsonPath("$.toUserId").value(user2Id))
                .andExpect(jsonPath("$.amount").value(transferAmount1))
                .andExpect(jsonPath("$.type").value(TRANSFER.name()))
                .andExpect(jsonPath("$.status").value(COMPLETED.name()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.completedAt", notNullValue()));

        // Проверяем баланс отправителя user1
        mockMvc.perform(get("/api/v1/user/account/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance")
                        .value(0L));

        // Проверяем баланс получателя user2
        mockMvc.perform(get("/api/v1/user/account/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user2AccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance")
                        .value(USER2_BALANCE + transferAmount1.doubleValue()));

        // Обрабатываем 2-ю транзакцию для перевода всех средств user1 к user2
        mockMvc.perform(post("/api/v1/transaction/transfer/commit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransactionRequest(transaction2Id))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", aMapWithSize(8)))
                .andExpect(jsonPath("$.id").value(transaction2Id))
                .andExpect(jsonPath("$.fromUserId").value(user1Id))
                .andExpect(jsonPath("$.toUserId").value(user2Id))
                .andExpect(jsonPath("$.amount").value(transferAmount2))
                .andExpect(jsonPath("$.type").value(TRANSFER.name()))
                .andExpect(jsonPath("$.status").value(FAILED.name()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.completedAt", notNullValue()));

        // Проверяем баланс отправителя user1
        mockMvc.perform(get("/api/v1/user/account/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance")
                        .value(0L));

        // Проверяем баланс получателя user2
        mockMvc.perform(get("/api/v1/user/account/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user2AccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance")
                        .value(USER2_BALANCE + transferAmount1.doubleValue()));
    }

    @Test
    @DisplayName("Повторный запрос на обработку транзакции не приводит к изменениям благодаря идемпотентности")
    void repeatedTransferRequest_hasNoEffect() throws Exception {
        BigDecimal transferAmount = BigDecimal.valueOf(100.00);

        // Инициируем транзакцию для перевода средств от user1 к user2
        MvcResult result = mockMvc.perform(post("/api/v1/transaction/transfer/init")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferRequest(user2Id, transferAmount))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", aMapWithSize(8)))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.fromUserId").value(user1Id))
                .andExpect(jsonPath("$.toUserId").value(user2Id))
                .andExpect(jsonPath("$.amount").value(transferAmount))
                .andExpect(jsonPath("$.type").value(TRANSFER.name()))
                .andExpect(jsonPath("$.status").value(PENDING.name()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.completedAt", nullValue()))
                .andReturn();

        Long transactionId = JsonPath.parse(result.getResponse().getContentAsString()).read("$.id", Long.class);

        // Обрабатываем транзакцию для перевода средств от user1 к user2
        mockMvc.perform(post("/api/v1/transaction/transfer/commit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransactionRequest(transactionId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", aMapWithSize(8)))
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.fromUserId").value(user1Id))
                .andExpect(jsonPath("$.toUserId").value(user2Id))
                .andExpect(jsonPath("$.amount").value(transferAmount))
                .andExpect(jsonPath("$.type").value(TRANSFER.name()))
                .andExpect(jsonPath("$.status").value(COMPLETED.name()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.completedAt", notNullValue()));

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

        // Повторно обрабатываем транзакцию для перевода средств от user1 к user2
        mockMvc.perform(post("/api/v1/transaction/transfer/commit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransactionRequest(transactionId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", aMapWithSize(8)))
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.fromUserId").value(user1Id))
                .andExpect(jsonPath("$.toUserId").value(user2Id))
                .andExpect(jsonPath("$.amount").value(transferAmount))
                .andExpect(jsonPath("$.type").value(TRANSFER.name()))
                .andExpect(jsonPath("$.status").value(COMPLETED.name()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.completedAt", notNullValue()));

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

        mockMvc.perform(post("/api/v1/transaction/transfer/init")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferRequest(user1Id, transferAmount))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Перевод суммы больше баланса должен быть отклонён")
    void transferFunds_insufficientBalance() throws Exception {
        BigDecimal transferAmount = BigDecimal.valueOf(USER1_BALANCE + 0.01);

        mockMvc.perform(post("/api/v1/transaction/transfer/init")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferRequest(user2Id, transferAmount))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Перевод суммы, равной 0, должен быть отклонён")
    void transferFunds_zeroAmount_shouldReturnBadRequest() throws Exception {
        BigDecimal transferAmount = BigDecimal.ZERO;

        mockMvc.perform(post("/api/v1/transaction/transfer/init")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferRequest(user2Id, transferAmount))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Перевод отрицательной суммы должен быть отклонён")
    void transferFunds_negativeAmount_shouldReturnBadRequest() throws Exception {
        BigDecimal transferAmount = new BigDecimal("-10.00");

        mockMvc.perform(post("/api/v1/transaction/transfer/init")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferRequest(user2Id, transferAmount))))
                .andExpect(status().isBadRequest());
    }

}
