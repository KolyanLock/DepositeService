package org.nikolait.assigment.userdeposit.service.impl;

import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.entity.User;
import org.nikolait.assigment.userdeposit.repository.UserRepository;
import org.nikolait.assigment.userdeposit.security.util.SecurityUtils;
import org.nikolait.assigment.userdeposit.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUserFetchEmailData() {
        return userRepository.findByIdFetchEmailData(SecurityUtils.getCurrentUserId()).orElseThrow(() ->
                new IllegalStateException("Current User not found in the database"));
    }

    @Override
    public User getCurrentUserFetchPhoneData() {
        return userRepository.findByIdFetchFetchPhoneData(SecurityUtils.getCurrentUserId()).orElseThrow(() ->
                new IllegalStateException("Current User not found in the database"));
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
