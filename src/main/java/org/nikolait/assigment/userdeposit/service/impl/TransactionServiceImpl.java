package org.nikolait.assigment.userdeposit.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikolait.assigment.userdeposit.config.DepositeConfig;
import org.nikolait.assigment.userdeposit.entity.Account;
import org.nikolait.assigment.userdeposit.entity.Transaction;
import org.nikolait.assigment.userdeposit.exception.TransferException;
import org.nikolait.assigment.userdeposit.repository.AccountRepository;
import org.nikolait.assigment.userdeposit.repository.TransactionRepository;
import org.nikolait.assigment.userdeposit.repository.UserRepository;
import org.nikolait.assigment.userdeposit.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static java.lang.Long.max;
import static java.lang.Long.min;
import static org.nikolait.assigment.userdeposit.emum.TransactionStatus.*;
import static org.nikolait.assigment.userdeposit.emum.TransactionType.TRANSFER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final DepositeConfig depositeConfig;

    @Override
    @Transactional
    public Transaction initTransfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        if (fromUserId.equals(toUserId)) {
            throw new TransferException("Cannot transfer money to yourself");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than 0");
        }
        if (!userRepository.existsById(toUserId)) {
            throw new EntityNotFoundException("Recipient user with id %d not found".formatted(toUserId));
        }

        Account fromAccount = getAccount(fromUserId);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new TransferException("Insufficient balance for transfer");
        }

        return transactionRepository.save(Transaction.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .amount(amount)
                .type(TRANSFER)
                .status(PENDING)
                .build());
    }

    /**
     * Можно было бы сделать с помощью многопоточных блокировок, но
     * пришлось бы вызвать этот метод, предварительно сделав блокировку
     * и использовать Redis Lock, если планируется масштабирование
     */
    @Override
    @Transactional(timeout = 10)
    public Transaction commitTransfer(Long transactionId, Long fromUserId) {
        Transaction transaction = getUserTransferTransactionWithLock(transactionId, fromUserId);

        if (transaction.getStatus() != PENDING) {
            return transaction;
        }

        Long toUserId = transaction.getToUserId();
        BigDecimal amount = transaction.getAmount();

        Long firstId = min(fromUserId, toUserId);
        Long secondId = max(fromUserId, toUserId);

        Account firstAccount = getUserAccountWithLock(firstId);
        Account secondAccount = getUserAccountWithLock(secondId);

        Account fromAccount = (firstId.equals(fromUserId)) ? firstAccount : secondAccount;
        Account toAccount = (firstId.equals(toUserId)) ? firstAccount : secondAccount;

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            transaction.setStatus(FAILED);
            transaction.setCompletedAt(Instant.now());
            log.info("Insufficient balance for transfer transaction id {} from user id {} to user id {}. Balase {}, amount {}",
                    transactionId, fromUserId, toUserId, amount, fromAccount.getBalance());
            return transaction;
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        transaction.setStatus(COMPLETED);
        transaction.setCompletedAt(Instant.now());
        return transaction;
    }

    @Override
    @Transactional
    public void accrueInterest(Long userId) {
        Account account = getUserAccountWithLock(userId);

        BigDecimal deposit = account.getDeposit();
        BigDecimal balance = account.getBalance();
        BigDecimal maxLimit = deposit.multiply(depositeConfig.getMaxRate());

        if (balance.compareTo(maxLimit) >= 0) {
            log.info("Limit reached for User with id {}", userId);
            return;
        }

        BigDecimal accruals = balance.multiply(depositeConfig.getInterestRate());

        if (balance.add(accruals).compareTo(maxLimit) >= 0) {
            account.setBalance(maxLimit);
            return;
        }

        account.setBalance(balance.add(accruals));
    }

    private Transaction getUserTransferTransactionWithLock(Long id, Long userId) {
        return transactionRepository.findByIdAndFromUserIdAndTypeWithLock(id, userId, TRANSFER)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Transfer Transaction with id %d not found for user with id %d".formatted(id, userId)
                ));
    }

    private Account getAccount(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Account for User with id %d not found".formatted(userId)
                ));
    }

    private Account getUserAccountWithLock(Long userId) {
        return accountRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Account for User with id %d not found".formatted(userId)
                ));
    }
}
