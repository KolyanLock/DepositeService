package org.nikolait.assigment.userdeposit.repository;

import org.nikolait.assigment.userdeposit.entity.PhoneData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {
    boolean existsByPhone(String phone);
}
