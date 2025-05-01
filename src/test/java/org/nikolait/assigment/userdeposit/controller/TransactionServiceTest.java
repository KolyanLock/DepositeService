package org.nikolait.assigment.userdeposit.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nikolait.assigment.userdeposit.IntegrationTestBase;
import org.nikolait.assigment.userdeposit.config.DepositeConfig;
import org.nikolait.assigment.userdeposit.entity.Account;
import org.nikolait.assigment.userdeposit.repository.AccountRepository;
import org.nikolait.assigment.userdeposit.service.AccountService;
import org.nikolait.assigment.userdeposit.service.TransactionService;
import org.nikolait.assigment.userdeposit.util.TestConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionServiceTest extends IntegrationTestBase {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DepositeConfig depositeConfig;

    @Test
    @DisplayName("Начисление процентов по вкладу")
    void accrueInterestForDepositeAccount() {
        BigDecimal rate = depositeConfig.getInterestRate();
        BigDecimal initialDeposit = BigDecimal.valueOf(TestConstants.USER1_DEPOSIT);
        BigDecimal expectedAccrual = initialDeposit.multiply(rate);
        BigDecimal maxRate = depositeConfig.getMaxRate().multiply(initialDeposit);

        assertThat(expectedAccrual).isLessThan(maxRate);

        transactionService.accrueInterest(user1Id);

        Account updated = accountRepository.findByUserId(user1Id).orElseThrow();

        assertThat(updated.getCapitalization()).isEqualByComparingTo(expectedAccrual);
        assertThat(updated.getBalance()).isEqualByComparingTo(initialDeposit.add(expectedAccrual));
    }

    @Test
    @DisplayName("Не начисляются средства для user3 с нулевым депозитом")
    void accrueInterestNoChangeForUserWithZeroDeposit() {
        transactionService.accrueInterest(user3Id);

        Account updated = accountRepository.findByUserId(user3Id).orElseThrow();
        assertThat(updated.getCapitalization()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(updated.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Ограничение начисления по лимиту")
    void accrueInterestCapAtMaxRate() {
        BigDecimal deposit = BigDecimal.valueOf(TestConstants.USER1_DEPOSIT);
        BigDecimal maxRate = depositeConfig.getMaxRate();
        BigDecimal maxLimit = deposit.multiply(maxRate);
        BigDecimal accruals = maxLimit.subtract(new BigDecimal("0.10"));

        Account account = accountRepository.findByUserId(user1Id).orElseThrow();
        account.setCapitalization(accruals);
        account.setBalance(account.getBalance().add(accruals));
        accountRepository.save(account);

        transactionService.accrueInterest(user1Id);

        Account updated = accountRepository.findByUserId(user1Id).orElseThrow();
        assertThat(updated.getCapitalization()).isEqualByComparingTo(maxLimit);
        assertThat(updated.getBalance()).isEqualByComparingTo(deposit.add(maxLimit));
    }
}
