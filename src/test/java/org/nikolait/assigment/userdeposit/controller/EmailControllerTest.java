package org.nikolait.assigment.userdeposit.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nikolait.assigment.userdeposit.IntegrationTestBase;
import org.nikolait.assigment.userdeposit.dto.EmailRequest;
import org.nikolait.assigment.userdeposit.elastic.EmailDataEs;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.nikolait.assigment.userdeposit.util.TestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmailControllerTest extends IntegrationTestBase {

    @Test
    @DisplayName("Добавление нового email для пользователя")
    void addEmail() throws Exception {
        mockMvc.perform(post("/api/v1/user/email")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EmailRequest(USER1_NEW_EMAIL)))
                )
                .andExpect(status().isCreated());

        await().untilAsserted(() -> mockMvc.perform(get("/api/v1/user/email")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].email", hasItem(USER1_NEW_EMAIL))));
    }

    @Test
    @DisplayName("Попытка добавить существующий email приводит к ошибке")
    void addDuplicateEmailShouldReturnConflict() throws Exception {
        mockMvc.perform(post("/api/v1/user/email")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EmailRequest(USER2_EMAIL)))
                )
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Изменение существующего email пользователя")
    void updateEmail() throws Exception {
        String body = mockMvc.perform(get("/api/v1/user/email")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmailDataEs[] existing = objectMapper.readValue(body, EmailDataEs[].class);
        Long id = existing[0].getId();
        String oldEmail = existing[0].getEmail();

        mockMvc.perform(put("/api/v1/user/email/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EmailRequest(USER1_NEW_EMAIL)))
                )
                .andExpect(status().isNoContent());

        await().untilAsserted(() -> mockMvc.perform(get("/api/v1/user/email")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].email", hasItem(USER1_NEW_EMAIL)))
                .andExpect(jsonPath("$[*].email", not(hasItem(oldEmail)))));
    }

    @Test
    @DisplayName("Удаление одного из email пользователя")
    void deleteEmail() throws Exception {
        String body = mockMvc.perform(get("/api/v1/user/email")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andReturn()
                .getResponse()
                .getContentAsString();
        EmailDataEs[] emails = objectMapper.readValue(body, EmailDataEs[].class);

        Long id = Stream.of(emails)
                .filter(e -> e.getEmail().equals(USER1_EMAIL))
                .findFirst()
                .orElseThrow()
                .getId();

        mockMvc.perform(delete("/api/v1/user/email/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                )
                .andExpect(status().isNoContent());

        await().untilAsserted(() -> mockMvc.perform(get("/api/v1/user/email")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].email", not(hasItem(USER1_EMAIL)))));
    }

    @Test
    @DisplayName("Попытка удалить email другого пользователя приводит к ошибке")
    void deleteOtherUserEmailShouldReturnNotFound() throws Exception {
        String body = mockMvc.perform(get("/api/v1/user/email")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user2AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andReturn()
                .getResponse()
                .getContentAsString();
        EmailDataEs[] emails = objectMapper.readValue(body, EmailDataEs[].class);

        Long otherUserEmailId = Stream.of(emails)
                .filter(e -> e.getEmail().equals(USER2_EMAIL))
                .findFirst()
                .orElseThrow()
                .getId();

        mockMvc.perform(delete("/api/v1/user/email/{id}", otherUserEmailId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Попытка удалить единственный email пользователя приводит к ошибке")
    void deleteOnlyEmailShouldFail() throws Exception {
        String body = mockMvc.perform(get("/api/v1/user/email")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user3AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andReturn()
                .getResponse()
                .getContentAsString();
        EmailDataEs[] emails = objectMapper.readValue(body, EmailDataEs[].class);

        Long id = emails[0].getId();

        mockMvc.perform(delete("/api/v1/user/email/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user3AccessToken)
                )
                .andExpect(status().isConflict());
    }


    @Test
    @DisplayName("Получение всех email текущего пользователя")
    void gatAllCurrentUserEmails() throws Exception {
        mockMvc.perform(get("/api/v1/user/email")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].email", hasItems(USER1_EMAIL, USER1_EMAIL2)));
    }

}
