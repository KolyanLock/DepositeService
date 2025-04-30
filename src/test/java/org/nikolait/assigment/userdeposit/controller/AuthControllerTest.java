package org.nikolait.assigment.userdeposit.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nikolait.assigment.userdeposit.IntegrationTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nikolait.assigment.userdeposit.util.TestConstants.*;

class AuthControllerTest extends IntegrationTestBase {

    @Test
    @DisplayName("Должен войти по основному email и паролю")
    void shouldLoginWithPrimaryEmail() {
        assertThat(authHelper.getAccessToken(USER1_EMAIL, USER1_PASSWORD)).isNotBlank();
    }

    @Test
    @DisplayName("Должен войти по второму email и паролю")
    void shouldLoginWithSecondaryEmail() {
        assertThat(authHelper.getAccessToken(USER1_EMAIL2, USER1_PASSWORD)).isNotBlank();
    }

    @Test
    @DisplayName("Должен войти по основному телефону и паролю")
    void shouldLoginWithPrimaryPhone() {
        assertThat(authHelper.getAccessToken(USER1_PHONE, USER1_PASSWORD)).isNotBlank();
    }

    @Test
    @DisplayName("Должен войти по второму телефону и паролю")
    void shouldLoginWithSecondaryPhone() {
        assertThat(authHelper.getAccessToken(USER1_PHONE2, USER1_PASSWORD)).isNotBlank();
    }

}
