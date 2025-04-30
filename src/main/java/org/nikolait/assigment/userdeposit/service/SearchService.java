package org.nikolait.assigment.userdeposit.service;

import org.nikolait.assigment.userdeposit.elastic.EmailDataEs;
import org.nikolait.assigment.userdeposit.elastic.PhoneDataEs;
import org.nikolait.assigment.userdeposit.elastic.UserEs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface SearchService {

    Page<UserEs> searchUsers(String name, String email, String phone, LocalDate dateOfBirth, Pageable pageable);

    UserEs getUserBasic(Long userId);

    UserEs getUserFull(Long userId);

    List<EmailDataEs> getUserEmails(Long userId);

    List<PhoneDataEs> getUserPhones(Long userId);

}
