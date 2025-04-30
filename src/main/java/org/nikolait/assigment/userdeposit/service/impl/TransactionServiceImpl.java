package org.nikolait.assigment.userdeposit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikolait.assigment.userdeposit.service.AccountService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl {

    private final AccountService accountService;

}
