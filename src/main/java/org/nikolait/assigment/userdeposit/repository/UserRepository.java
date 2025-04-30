package org.nikolait.assigment.userdeposit.repository;

import org.nikolait.assigment.userdeposit.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT DISTINCT u FROM User u JOIN FETCH u.emails e WHERE e.email = :login")
    Optional<User> findByEmailFetchEmailData(String login);

    @Query("SELECT DISTINCT u FROM User u JOIN FETCH u.phones p WHERE p.phone = :login")
    Optional<User> findByPhoneFetchPhoneData(String login);

    @EntityGraph(attributePaths = "emails")
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdFetchEmailData(Long id);

    @EntityGraph(attributePaths = "phones")
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdFetchFetchPhoneData(Long id);

    @EntityGraph(attributePaths = {"emails", "phones"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdFetchEmailsAndPhones(Long id);

    @EntityGraph(attributePaths = {"emails", "phones"})
    @Query("SELECT u FROM User u")
    List<User> findAllFetchEmailsAndPhones();

    @Query("select u.id from User u")
    List<Long> findAllIds();
}
