package org.nikolait.assigment.userdeposit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nikolait.assigment.userdeposit.validation.EmailOrPhone;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank
    @EmailOrPhone
    private String login;

    @NotBlank
    private String password;

}
