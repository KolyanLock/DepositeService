package org.nikolait.assigment.userdeposit.repository;

import org.nikolait.assigment.userdeposit.entity.EmailData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmailDataRepository extends JpaRepository<EmailData, Long> {

    @Query(" SELECT e FROM EmailData e JOIN FETCH e.user u WHERE e.email = :email")
    Optional<EmailData> findByEmailFetchUser(String email);

    Optional<EmailData> findByIdAndUserId(Long id, Long userId);

    List<EmailData> findAllByUserId(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    int deleteByIdAndUserId(Long id, Long userId);

    boolean existsByEmail(String email);
}
