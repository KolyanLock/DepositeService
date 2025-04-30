package org.nikolait.assigment.userdeposit.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nikolait.assigment.userdeposit.IntegrationTestBase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.nikolait.assigment.userdeposit.util.TestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends IntegrationTestBase {

    @Test
    @DisplayName("Должен вернуть basic-представление пользователя")
    void getCurrentUserBasic() throws Exception {
        mockMvc.perform(get("/api/v1/user/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(5)))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(USER1_NAME))
                .andExpect(jsonPath("$.dateOfBirth").value(USER1_DATE_OF_BIRTH))
                .andExpect(jsonPath("$.emails").doesNotExist())
                .andExpect(jsonPath("$.phones").doesNotExist());
    }


    @Test
    @DisplayName("Должен вернуть полный пользователей с emails и phones")
    void getCurrentUserFull() throws Exception {
        mockMvc.perform(get("/api/v1/user/me/full")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(5)))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(USER1_NAME))
                .andExpect(jsonPath("$.dateOfBirth").value(USER1_DATE_OF_BIRTH))
                .andExpect(jsonPath("$.emails", hasSize(2)))
                .andExpect(jsonPath("$.emails[*].email")
                        .value(containsInAnyOrder(
                                USER1_EMAIL,
                                USER1_EMAIL2)))
                .andExpect(jsonPath("$.phones", hasSize(2)))
                .andExpect(jsonPath("$.phones[*].phone")
                        .value(containsInAnyOrder(
                                USER1_PHONE,
                                USER1_PHONE2)));
    }

    @Test
    @DisplayName("Поиск без фильтров возвращает всех пользователей с пагинацией")
    void searchAllWithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/user/search")
                        .param("page", "0")
                        .param("size", "2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    @DisplayName("Поиск по префиксу name LIKE '{param}%'")
    void searchByNameLike() throws Exception {
        mockMvc.perform(get("/api/v1/user/search")
                        .param("name", "Мария")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", equalTo(USER2_NAME)));
    }

    @Test
    @DisplayName("Поиск по точному совпадению email")
    void searchByEmailExact() throws Exception {
        mockMvc.perform(get("/api/v1/user/search")
                        .param("email", USER2_EMAIL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", equalTo(USER2_NAME)));
    }

    @Test
    @DisplayName("поиск по точному совпадению phone")
    void searchByPhoneExact() throws Exception {
        mockMvc.perform(get("/api/v1/user/search")
                        .param("phone", USER3_PHONE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", equalTo(USER3_NAME)));
    }

    @Test
    @DisplayName("Поиск по дате рождения >= dateOfBirth")
    void searchByDateOfBirthGreaterOrEqual() throws Exception {
        mockMvc.perform(get("/api/v1/user/search")
                        .param("dateOfBirth", "1990-01-01")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1AccessToken)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].dateOfBirth",
                        everyItem(not(is(lessThan("1990-01-01"))))))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder(USER1_NAME, USER3_NAME)));
    }
}
