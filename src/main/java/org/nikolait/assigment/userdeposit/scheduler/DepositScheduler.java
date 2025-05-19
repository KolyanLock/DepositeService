package org.nikolait.assigment.userdeposit.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikolait.assigment.userdeposit.repository.UserRepository;
import org.nikolait.assigment.userdeposit.service.AdvisoryLockService;
import org.nikolait.assigment.userdeposit.service.TransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.nikolait.assigment.userdeposit.emum.AdvisoryLockType.TRIGGER_ACCRUAL;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositScheduler {

    private final AdvisoryLockService lockService;
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    /**
     * В пределах 100_000 пользователей точно безопасно findAllIds.
     * Если пользователей миллионы, тогда можно stream или порциями.
     */
    @Scheduled(
            fixedRateString = "${deposit.scheduler-interval}",
            initialDelayString = "${deposit.scheduler-init-delay}"
    )
    public void triggerAccrual() {
        if (!lockService.tryLock(TRIGGER_ACCRUAL)) {
            return;
        }
        log.info("Starting DepositScheduler triggerAccrual >>>");
        userRepository.findAllIds().forEach(userId -> {
            try {
                transactionService.accrueInterest(userId);
            } catch (Exception e) {
                log.error("Failed to accrue interest for user with id {}", userId, e);
            }
        });
        log.info("Finished DepositScheduler triggerAccrual <<<");
    }

}
