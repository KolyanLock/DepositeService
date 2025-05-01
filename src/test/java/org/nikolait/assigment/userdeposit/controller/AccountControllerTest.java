package org.nikolait.assigment.userdeposit.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nikolait.assigment.userdeposit.IntegrationTestBase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.nikolait.assigment.userdeposit.util.TestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

}
