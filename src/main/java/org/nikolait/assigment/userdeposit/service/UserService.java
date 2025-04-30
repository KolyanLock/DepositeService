package org.nikolait.assigment.userdeposit.service;

import org.nikolait.assigment.userdeposit.entity.User;

public interface UserService {

    User getCurrentUserFetchEmailData();

    User getCurrentUserFetchPhoneData();

    User saveUser(User user);

}
