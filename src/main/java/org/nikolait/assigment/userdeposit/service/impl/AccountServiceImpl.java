package org.nikolait.assigment.userdeposit.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikolait.assigment.userdeposit.config.DepositeConfig;
import org.nikolait.assigment.userdeposit.entity.Account;
import org.nikolait.assigment.userdeposit.exception.TransferException;
import org.nikolait.assigment.userdeposit.repository.AccountRepository;
import org.nikolait.assigment.userdeposit.security.util.SecurityUtils;
import org.nikolait.assigment.userdeposit.service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static java.lang.Long.max;
import static java.lang.Long.min;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final DepositeConfig depositeConfig;


    @Override
    public Account getCurrentUserAccount() {
        return accountRepository.findByUserId(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new IllegalStateException("Account not found for current user"));
    }

    /**
     * Может быть в больших проектах лучше оптимистичные блокировки,
     * но тут вся логика ясна и не должно быть deadlock.
     * Можно было бы сделать с помощью многопоточных блокировок, но
     * пришлось бы вызвать этот метод, предварительно сделав блокировку
     * и использовать Redis Lock, если планируется масштабирование
     */
    @Override
    @Transactional(timeout = 10)
    public void transfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        if (fromUserId.equals(toUserId)) {
            throw new TransferException("Cannot transfer money to yourself");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than 0");
        }

        Long firstId = min(fromUserId, toUserId);
        Long secondId = max(fromUserId, toUserId);

        Account firstAccount = getUserAccountWithLock(firstId);
        Account secondAccount = getUserAccountWithLock(secondId);

        Account fromAccount = (firstId.equals(fromUserId)) ? firstAccount : secondAccount;
        Account toAccount = (firstId.equals(toUserId)) ? firstAccount : secondAccount;

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new TransferException("Insufficient balance for transfer");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
    }

    @Override
    @Transactional
    public void accrueInterest(Long userId) {
        Account account = getUserAccountWithLock(userId);

        BigDecimal deposit = account.getDeposit();
        BigDecimal capitalization = account.getCapitalization();
        BigDecimal balance = account.getBalance();
        BigDecimal accruals = deposit.multiply(depositeConfig.getInterestRate());
        BigDecimal maxLimit = account.getDeposit().multiply(depositeConfig.getMaxRate());

        if (capitalization.compareTo(maxLimit) > 0) {
            log.info("Capitalization limit exceeded for user with id {}", userId);
            return;
        }

        if (capitalization.add(accruals).compareTo(maxLimit) > 0) {
            accruals = maxLimit.subtract(capitalization);
        }

        account.setCapitalization(capitalization.add(accruals));
        account.setBalance(balance.add(accruals));
    }

    private Account getUserAccountWithLock(Long userId) {
        return accountRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Account for User with id %d not found".formatted(userId)
                ));
    }

}
