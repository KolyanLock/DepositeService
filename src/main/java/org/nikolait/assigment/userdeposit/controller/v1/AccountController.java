package org.nikolait.assigment.userdeposit.controller.v1;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.dto.AccountResponse;
import org.nikolait.assigment.userdeposit.dto.TransferRequest;
import org.nikolait.assigment.userdeposit.mapper.AccountMapper;
import org.nikolait.assigment.userdeposit.security.util.SecurityUtils;
import org.nikolait.assigment.userdeposit.service.AccountService;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/transfer")
    public void transferFunds(@RequestBody TransferRequest request) {
        Long fromUserId = SecurityUtils.getCurrentUserId();
        accountService.transfer(fromUserId, request.getUserId(), request.getValue());
    }
}
