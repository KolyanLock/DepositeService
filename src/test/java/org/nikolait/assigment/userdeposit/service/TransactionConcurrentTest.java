package org.nikolait.assigment.userdeposit.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nikolait.assigment.userdeposit.IntegrationTestBase;
import org.nikolait.assigment.userdeposit.entity.Account;
import org.nikolait.assigment.userdeposit.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.nikolait.assigment.userdeposit.util.TestConstants.*;

class TransactionConcurrentTest extends IntegrationTestBase {

    private static final int TOTAL_THEAD_COUNT = 8;
    private static final int TRANSFERS_PER_THREAD = 10;

    private static final int TIMEOUT_SECONDS = 30;

    private static final BigDecimal user1InitialDeposit = BigDecimal.valueOf(USER1_DEPOSIT);
    private static final BigDecimal user2InitialDeposit = BigDecimal.valueOf(USER2_DEPOSIT);
    private static final BigDecimal user3InitialDeposit = BigDecimal.valueOf(USER3_DEPOSIT);

    private static final BigDecimal TRANSFER_12_AMOUNT = new BigDecimal("10.00");
    private static final BigDecimal TRANSFER_23_AMOUNT = new BigDecimal("3.50");
    private static final BigDecimal TRANSFER_31_AMOUNT = new BigDecimal("1.75");

    private static ExecutorService executorService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void initExecutor() {
        executorService = Executors.newFixedThreadPool(TOTAL_THEAD_COUNT);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void concurrentTransfers_shouldMaintainCorrectBalances() throws Exception {
        List<Future<?>> futures = new ArrayList<>(TOTAL_THEAD_COUNT);

        // Запуск потоков для переводов
        submitTransferTasks(futures);

        // Ожидание завершения
        for (Future<?> future : futures) {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        // Получение данных
        Account account1 = accountRepository.findByUserId(user1Id).orElseThrow();
        Account account2 = accountRepository.findByUserId(user2Id).orElseThrow();
        Account account3 = accountRepository.findByUserId(user3Id).orElseThrow();

        int totalCycles = TOTAL_THEAD_COUNT * TRANSFERS_PER_THREAD;

        BigDecimal totalTransfers1To2 = TRANSFER_12_AMOUNT.multiply(BigDecimal.valueOf(totalCycles));
        BigDecimal totalTransfers2To3 = TRANSFER_23_AMOUNT.multiply(BigDecimal.valueOf(totalCycles));
        BigDecimal totalTransfers3To1 = TRANSFER_31_AMOUNT.multiply(BigDecimal.valueOf(totalCycles));

        BigDecimal expectedUser1Balance = user1InitialDeposit
                .subtract(totalTransfers1To2)
                .add(totalTransfers3To1);
        BigDecimal expectedUser2Balance = user2InitialDeposit
                .add(totalTransfers1To2)
                .subtract(totalTransfers2To3);
        BigDecimal expectedUser3Balance = user3InitialDeposit
                .add(totalTransfers2To3)
                .subtract(totalTransfers3To1);

        // Проверки
        assertAll(
                // Проверка балансов
                () -> assertThat(account1.getBalance()).isEqualByComparingTo(expectedUser1Balance),
                () -> assertThat(account2.getBalance()).isEqualByComparingTo(expectedUser2Balance),
                () -> assertThat(account3.getBalance()).isEqualByComparingTo(expectedUser3Balance)
        );

    }

    private void submitTransferTasks(List<Future<?>> futures) {
        for (int i = 0; i < TOTAL_THEAD_COUNT; i++) {
            futures.add(executorService.submit(() -> {
                for (int j = 0; j < TRANSFERS_PER_THREAD; j++) {
                    Long transactionId1 = transactionService.initTransfer(user1Id, user2Id, TRANSFER_12_AMOUNT).getId();
                    transactionService.commitTransfer(transactionId1, user1Id);
                    Long transactionId2 = transactionService.initTransfer(user2Id, user3Id, TRANSFER_23_AMOUNT).getId();
                    transactionService.commitTransfer(transactionId2, user2Id);
                    Long transactionId3 = transactionService.initTransfer(user3Id, user1Id, TRANSFER_31_AMOUNT).getId();
                    transactionService.commitTransfer(transactionId3, user3Id);
                }
            }));
        }
    }

}
