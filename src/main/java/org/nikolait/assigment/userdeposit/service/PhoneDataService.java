package org.nikolait.assigment.userdeposit.service;

public interface PhoneDataService {

    void createPhone(String phone);

    void updatePhone(Long id, String phone);

    void deletePhone(Long id);
}
