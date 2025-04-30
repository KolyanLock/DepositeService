package org.nikolait.assigment.userdeposit.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nikolait.assigment.userdeposit.IntegrationTestBase;
import org.nikolait.assigment.userdeposit.dto.PhoneRequest;
import org.nikolait.assigment.userdeposit.elastic.PhoneDataEs;
import org.nikolait.assigment.userdeposit.util.TestConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.nikolait.assigment.userdeposit.util.TestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PhoneControllerTest extends IntegrationTestBase {

    @Test
    @DisplayName("Добавление нового phone для пользователя")
    void addPhone() throws Exception {
        mockMvc.perform(post("/api/v1/user/phone")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PhoneRequest(USER1_NEW_PHONE)))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/user/phone")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].phone", hasItem(TestConstants.USER1_NEW_PHONE)));
    }

    @Test
    @DisplayName("Попытка добавить существующий phone приводит к ошибке")
    void addDuplicatePhoneShouldReturnConflict() throws Exception {
        mockMvc.perform(post("/api/v1/user/phone")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PhoneRequest(TestConstants.USER2_PHONE)))
                )
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Изменение существующего phone пользователя")
    void updatePhone() throws Exception {
        String body = mockMvc.perform(get("/api/v1/user/phone")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PhoneDataEs[] existing = objectMapper.readValue(body, PhoneDataEs[].class);
        Long id = existing[0].getId();
        String oldPhone = existing[0].getPhone();

        mockMvc.perform(put("/api/v1/user/phone/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PhoneRequest(USER1_NEW_PHONE)))
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/user/phone")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].phone", hasItem(USER1_NEW_PHONE)))
                .andExpect(jsonPath("$[*].phone", not(hasItem(oldPhone))));
    }

    @Test
    @DisplayName("Удаление одного из phone пользователя")
    void deletePhone() throws Exception {
        String body = mockMvc.perform(get("/api/v1/user/phone")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andReturn()
                .getResponse()
                .getContentAsString();
        PhoneDataEs[] phones = objectMapper.readValue(body, PhoneDataEs[].class);

        Long id = Stream.of(phones)
                .filter(e -> e.getPhone().equals(USER1_PHONE))
                .findFirst()
                .orElseThrow()
                .getId();

        mockMvc.perform(delete("/api/v1/user/phone/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/user/phone")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].phone", not(hasItem(USER1_PHONE))));
    }

    @Test
    @DisplayName("Попытка удалить phone другого пользователя приводит к ошибке")
    void deleteOtherUserPhoneShouldReturnNotFound() throws Exception {
        String body = mockMvc.perform(get("/api/v1/user/phone")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user2AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andReturn()
                .getResponse()
                .getContentAsString();
        PhoneDataEs[] phones = objectMapper.readValue(body, PhoneDataEs[].class);

        Long otherUserPhoneId = Stream.of(phones)
                .filter(p -> p.getPhone().equals(USER2_PHONE))
                .findFirst()
                .orElseThrow()
                .getId();

        mockMvc.perform(delete("/api/v1/user/phone/{id}", otherUserPhoneId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Попытка удалить единственный phone пользователя")
    void deleteOnlyPhoneShouldFail() throws Exception {
        String body = mockMvc.perform(get("/api/v1/user/phone")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user3AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andReturn()
                .getResponse()
                .getContentAsString();
        PhoneDataEs[] phones = objectMapper.readValue(body, PhoneDataEs[].class);

        Long id = phones[0].getId();

        mockMvc.perform(delete("/api/v1/user/phone/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user3AccessToken)
                )
                .andExpect(status().isConflict());
    }


    @Test
    @DisplayName("Получение всех phone текущего пользователя")
    void gatAllCurrentUserPhones() throws Exception {
        mockMvc.perform(get("/api/v1/user/phone")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].phone", hasItems(USER1_PHONE, USER1_PHONE2)));
    }

}
