package org.nikolait.assigment.userdeposit.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nikolait.assigment.userdeposit.IntegrationTestBase;
import org.nikolait.assigment.userdeposit.entity.Account;
import org.nikolait.assigment.userdeposit.repository.AccountRepository;
import org.nikolait.assigment.userdeposit.scheduler.DepositScheduler;
import org.nikolait.assigment.userdeposit.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.nikolait.assigment.userdeposit.util.TestConstants.*;

class TransactionConcurrentTest extends IntegrationTestBase {

    private static final int TOTAL_THEAD_COUNT = 8;
    private static final int TRANSFER_THEAD_COUNT = 6;
    private static final int TRANSFERS_PER_THREAD = 10;
    private static final int ACCRUAL_THREAD_COUNT = 2;

    private static final int TIMEOUT_SECONDS = 30;

    private static final BigDecimal user1InitialDeposit = BigDecimal.valueOf(USER1_DEPOSIT);
    private static final BigDecimal user2InitialDeposit = BigDecimal.valueOf(USER2_DEPOSIT);
    private static final BigDecimal user3InitialDeposit = BigDecimal.valueOf(USER3_DEPOSIT);

    private static final BigDecimal TRANSFER_12_AMOUNT = new BigDecimal("10.00");
    private static final BigDecimal TRANSFER_23_AMOUNT = new BigDecimal("3.50");
    private static final BigDecimal TRANSFER_31_AMOUNT = new BigDecimal("1.75");

    private static ExecutorService executorService;

    @Autowired
    private DepositScheduler depositScheduler;

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
    void concurrentTransfersWithAccrual_shouldMaintainCorrectBalances() throws Exception {
        List<Future<?>> futures = new ArrayList<>(TOTAL_THEAD_COUNT);

        // Запуск потоков для переводов
        submitTransferTasks(futures);

        // Запуск потоков для начисления процентов
        submitDepositSchedulerTasks(futures);

        // Ожидание завершения
        for (Future<?> future : futures) {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        // Получение данных
        Account user1 = accountRepository.findByUserId(user1Id).orElseThrow();
        Account user2 = accountRepository.findByUserId(user2Id).orElseThrow();
        Account user3 = accountRepository.findByUserId(user3Id).orElseThrow();

        // Максимальные капитализации (100% от депозита)
        BigDecimal maxUser1Cap = user1InitialDeposit.multiply(BigDecimal.ONE);
        BigDecimal maxUser2Cap = user2InitialDeposit.multiply(BigDecimal.ONE);

        // Реальные капитализации
        BigDecimal actualUser1Cap = user1.getCapitalization().min(maxUser1Cap);
        BigDecimal actualUser2Cap = user2.getCapitalization().min(maxUser2Cap);

        // Расчёты трансферов
        int totalCycles = TRANSFER_THEAD_COUNT * TRANSFERS_PER_THREAD;
        BigDecimal totalTransfers1To2 = TRANSFER_12_AMOUNT.multiply(BigDecimal.valueOf(totalCycles));
        BigDecimal totalTransfers2To3 = TRANSFER_23_AMOUNT.multiply(BigDecimal.valueOf(totalCycles));
        BigDecimal totalTransfers3To1 = TRANSFER_31_AMOUNT.multiply(BigDecimal.valueOf(totalCycles));

        // Ожидаемые балансы
        BigDecimal expectedUser1Balance = BigDecimal.valueOf(USER1_BALANCE)
                .add(actualUser1Cap)
                .add(totalTransfers3To1)
                .subtract(totalTransfers1To2);

        BigDecimal expectedUser2Balance = BigDecimal.valueOf(USER2_BALANCE)
                .add(actualUser2Cap)
                .add(totalTransfers1To2)
                .subtract(totalTransfers2To3);

        BigDecimal expectedUser3Balance = BigDecimal.valueOf(USER3_BALANCE)
                .add(totalTransfers2To3)
                .subtract(totalTransfers3To1);

        // Проверки
        assertAll(
                // Проверка балансов
                () -> assertEquals(0, expectedUser1Balance.compareTo(user1.getBalance())),
                () -> assertEquals(0, expectedUser2Balance.compareTo(user2.getBalance())),
                () -> assertEquals(0, expectedUser3Balance.compareTo(user3.getBalance())),

                // Проверка капитализации
                () -> assertEquals(0, BigDecimal.valueOf(USER1_CAPITALIZATION)
                        .compareTo(user1.getCapitalization())),
                () -> assertEquals(0, BigDecimal.valueOf(USER2_CAPITALIZATION)
                        .compareTo(user2.getCapitalization())),
                () -> assertEquals(0, BigDecimal.valueOf(USER3_CAPITALIZATION)
                        .compareTo(user3.getCapitalization())),

                // Проверка общего баланса
                () -> {
                    BigDecimal totalAfter = user1.getBalance()
                            .add(user2.getBalance())
                            .add(user3.getBalance());

                    BigDecimal expectedTotal = user1InitialDeposit
                            .add(user2InitialDeposit)
                            .add(user3InitialDeposit)
                            .add(actualUser1Cap)
                            .add(actualUser2Cap);

                    assertEquals(0, expectedTotal.compareTo(totalAfter));
                }
        );

    }

    private void submitTransferTasks(List<Future<?>> futures) {
        for (int i = 0; i < TRANSFER_THEAD_COUNT; i++) {
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

    private void submitDepositSchedulerTasks(List<Future<?>> futures) {
        for (int i = 0; i < ACCRUAL_THREAD_COUNT; i++) {
            futures.add(executorService.submit(() -> {
                for (int j = 0; j < 5; j++) {
                    depositScheduler.triggerAccrual();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) { /* ignore */ }
                }
            }));
        }
    }

}
