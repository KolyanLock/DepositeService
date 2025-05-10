package org.nikolait.assigment.userdeposit.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nikolait.assigment.userdeposit.IntegrationTestBase;
import org.nikolait.assigment.userdeposit.config.DepositeConfig;
import org.nikolait.assigment.userdeposit.entity.Account;
import org.nikolait.assigment.userdeposit.repository.AccountRepository;
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
        Account before = accountRepository.findByUserId(user1Id).orElseThrow();
        BigDecimal balanceBefore = before.getBalance();
        BigDecimal maxLimit = before.getDeposit().multiply(depositeConfig.getMaxRate());
        BigDecimal accrual = balanceBefore.multiply(depositeConfig.getInterestRate());

        BigDecimal expectedBalance = balanceBefore.add(accrual).min(maxLimit);

        transactionService.accrueInterest(user1Id);

        Account after = accountRepository.findByUserId(user1Id).orElseThrow();

        assertThat(after.getBalance()).isEqualByComparingTo(expectedBalance);
    }

    @Test
    @DisplayName("Не начисляются средства для user3 с нулевым депозитом")
    void accrueInterestNoChangeForUserWithZeroDeposit() {
        Account before = accountRepository.findByUserId(user3Id).orElseThrow();
        assertThat(before.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        transactionService.accrueInterest(user3Id);

        Account after = accountRepository.findByUserId(user3Id).orElseThrow();
        assertThat(after.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Ограничение начисления по лимиту")
    void accrueInterestCapAtMaxRate() {
        Account before = accountRepository.findByUserId(user1Id).orElseThrow();
        BigDecimal maxLimit = before.getDeposit().multiply(depositeConfig.getMaxRate());

        // Устанавливаем баланс чуть меньше лимита
        before.setBalance(maxLimit.subtract(new BigDecimal("0.10")));
        accountRepository.save(before);

        transactionService.accrueInterest(user1Id);

        Account after = accountRepository.findByUserId(user1Id).orElseThrow();
        assertThat(after.getBalance()).isEqualByComparingTo(maxLimit);
    }
}
