package org.nikolait.assigment.userdeposit.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.nikolait.assigment.userdeposit.dto.TransactionResponse;
import org.nikolait.assigment.userdeposit.entity.Transaction;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface TransactionService {

    Transaction initTransfer(Long fromUserId, Long toUserId, BigDecimal amount);

    Transaction commitTransfer(Long transactionId, Long fromUserId);

    void accrueInterest(Long userId);
}
