package org.nikolait.assigment.userdeposit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikolait.assigment.userdeposit.entity.Account;
import org.nikolait.assigment.userdeposit.repository.AccountRepository;
import org.nikolait.assigment.userdeposit.security.util.SecurityUtils;
import org.nikolait.assigment.userdeposit.service.AccountService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public Account getCurrentUserAccount() {
        return accountRepository.findByUserId(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new IllegalStateException("Account not found for current user"));
    }

}
