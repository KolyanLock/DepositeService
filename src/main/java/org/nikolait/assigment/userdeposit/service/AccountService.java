package org.nikolait.assigment.userdeposit.service;

import org.nikolait.assigment.userdeposit.entity.Account;

import java.math.BigDecimal;

public interface AccountService {

    Account getCurrentUserAccount();

    void transfer(Long fromUserId, Long toUserId, BigDecimal amount);

    void accrueInterest(Long userId);

}
