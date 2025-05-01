package org.nikolait.assigment.userdeposit.repository;

import jakarta.persistence.LockModeType;
import org.nikolait.assigment.userdeposit.emum.TransactionType;
import org.nikolait.assigment.userdeposit.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Transaction t WHERE t.id = :id AND t.fromUserId = :fromUserId AND t.type = :type")
    Optional<Transaction> findByIdAndFromUserIdAndTypeWithLock(Long id, Long fromUserId, TransactionType type);

}
