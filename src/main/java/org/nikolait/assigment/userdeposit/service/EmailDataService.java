package org.nikolait.assigment.userdeposit.service;

public interface EmailDataService {

    void createEmail(String email);

    void updateEmail(Long id, String email);

    void deleteEmail(Long id);
}
