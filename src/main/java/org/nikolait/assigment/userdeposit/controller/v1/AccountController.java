package org.nikolait.assigment.userdeposit.controller.v1;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.dto.AccountResponse;
import org.nikolait.assigment.userdeposit.mapper.AccountMapper;
import org.nikolait.assigment.userdeposit.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/account")
@SecurityRequirement(name = AUTHORIZATION)
public class AccountController {
    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @GetMapping("/me")
    public AccountResponse getMyAccount() {
        return accountMapper.toResponse(accountService.getCurrentUserAccount());
    }

}
