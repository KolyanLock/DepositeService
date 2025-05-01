package org.nikolait.assigment.userdeposit.dto;

import org.nikolait.assigment.userdeposit.emum.TransactionStatus;
import org.nikolait.assigment.userdeposit.emum.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long fromUserId,
        Long toUserId,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        Instant createdAt,
        Instant completedAt
) {
}
