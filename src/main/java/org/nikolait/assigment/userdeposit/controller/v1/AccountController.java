package org.nikolait.assigment.userdeposit.controller.v1;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikolait.assigment.userdeposit.dto.AccountResponse;
import org.nikolait.assigment.userdeposit.dto.TransferRequest;
import org.nikolait.assigment.userdeposit.mapper.AccountMapper;
import org.nikolait.assigment.userdeposit.security.util.SecurityUtils;
import org.nikolait.assigment.userdeposit.service.AccountService;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
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
    public void transferFunds(@RequestBody @Valid TransferRequest request) {
        try {
            Long fromUserId = SecurityUtils.getCurrentUserId();
            accountService.transfer(fromUserId, request.getUserId(), request.getValue());
        } catch (Exception e) {
            log.error("Executing transfer to user id {} for amount {} failed! Error: {}, message: {}",
                   request.getUserId(), request.getValue(), e.getClass().getName(), e.getMessage(), e);
            throw e;
        }
    }
}
